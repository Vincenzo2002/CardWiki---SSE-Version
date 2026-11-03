package at.ac.tuwien.sepm.groupphase.backend.profiles.dev;

import at.ac.tuwien.sepm.groupphase.backend.config.security.SecurityConfig;
import at.ac.tuwien.sepm.groupphase.backend.config.security.SecurityProps;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Profile("dev")
@RestController
@RequestMapping(value = "/api/v1/auth/providers/fake")
public class DevAuthProviderEndpoint {

    private final SecurityProps securityProps;

    public DevAuthProviderEndpoint(final SecurityProps securityProps) {
        this.securityProps = securityProps;
    }

    @GetMapping("/{username}")
    public RedirectView get(@PathVariable final String username, final HttpServletRequest request, final HttpServletResponse response){
        SecurityConfig.addTokenCookie(username, securityProps, request, response);
        return new RedirectView(securityProps.getFrontendSuccessURL());
    }
}

