package at.ac.tuwien.sepm.groupphase.benchmarks;

import at.ac.tuwien.sepm.groupphase.backend.entity.Card;
import at.ac.tuwien.sepm.groupphase.backend.entity.Deck;
import at.ac.tuwien.sepm.groupphase.backend.entity.Image;
import at.ac.tuwien.sepm.groupphase.backend.entity.RevisionEdit;
import at.ac.tuwien.sepm.groupphase.backend.entity.User;
import at.ac.tuwien.sepm.groupphase.backend.repository.CardRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.DeckRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import at.ac.tuwien.sepm.groupphase.backend.service.impl.SimpleDeckService;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.openjdk.jmh.annotations.*;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class SimpleDeckServiceBenchmark {

    @Param({"0", "1", "10", "100", "1000"})
    public int size;

    private SimpleDeckService service;
    private Deck sourceDeck;

    @Setup
    public void setup() {
        ((Logger) LoggerFactory.getLogger(SimpleDeckService.class)).setLevel(Level.WARN);

        User sourceAuthor = new User(2L);
        User currentUser = new User(1L);
        sourceDeck = new Deck();
        sourceDeck.setId(42L);
        sourceDeck.setName("Source deck");
        sourceDeck.setCreatedBy(sourceAuthor);

        Image frontImage = new Image();
        frontImage.setFilename("front.png");
        Image backImage = new Image();
        backImage.setFilename("back.png");

        List<RevisionEdit> sourceRevisions = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Card card = new Card();
            card.setId((long) i + 1);
            card.setDeck(sourceDeck);

            RevisionEdit revision = new RevisionEdit();
            revision.setId((long) i + 10000);
            revision.setCard(card);
            revision.setCreatedBy(sourceAuthor);
            revision.setMessage("Source revision");
            revision.setTextFront("Front " + i);
            revision.setTextBack("Back " + i);
            revision.setImageFront(frontImage);
            revision.setImageBack(backImage);
            card.setLatestRevision(revision);
            sourceDeck.getCards().add(card);
            sourceRevisions.add(revision);
        }
        Optional<Deck> sourceLookup = Optional.of(sourceDeck);

        // Only the calls used by copy are supported. No database, generated
        // IDs, persistence callbacks, or invocation history are simulated.
        DeckRepository decks = (DeckRepository) Proxy.newProxyInstance(
            DeckRepository.class.getClassLoader(),
            new Class<?>[]{DeckRepository.class},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "findById":
                        return sourceLookup;
                    case "save":
                        return args[0];
                    default:
                        throw new UnsupportedOperationException(method.getName());
                }
            });

        CardRepository cards = (CardRepository) Proxy.newProxyInstance(
            CardRepository.class.getClassLoader(),
            new Class<?>[]{CardRepository.class},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "findLatestEditRevisionsByDeck_Id":
                        // Streams are single-use: return a fresh one each time.
                        return sourceRevisions.stream();
                    case "saveAll":
                        return args[0];
                    case "flush":
                        return null;
                    default:
                        throw new UnsupportedOperationException(method.getName());
                }
            });

        UserService users = (UserService) Proxy.newProxyInstance(
            UserService.class.getClassLoader(),
            new Class<?>[]{UserService.class},
            (proxy, method, args) -> {
                if ("loadCurrentUserOrThrow".equals(method.getName())) {
                    return currentUser;
                }
                throw new UnsupportedOperationException(method.getName());
            });

        // copy does not use the category, revision, or progress service/repositories.
        service = new SimpleDeckService(decks, users, null, cards, null, null);
    }

    @Benchmark
    public Deck copyDeck() {
        // A new destination prevents cards from accumulating across invocations.
        // Destination creation is included in the measured operation.
        Deck destination = new Deck();
        destination.setName("Copied deck");
        return service.copy(sourceDeck.getId(), destination);
    }
}
