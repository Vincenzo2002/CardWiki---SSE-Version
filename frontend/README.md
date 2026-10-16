# CardWiki Frontend

## First Steps

Use Node.js **14.21.3** and its bundled npm **6.14.18** for this legacy Angular project. The repository's `.nvmrc` records this version, matching the Node 14 deployment workflow. With nvm installed, run `nvm install` and `nvm use` from the repository root.

Run the following commands from **`frontend/`**, which contains `package.json`:

```sh
npm ci
npm start
```

`npm ci` installs the versions in `package-lock.json` and runs the postinstall step that creates the ignored `src/environments/config.prod.ts`. The project scripts use the local Angular CLI.

On Apple Silicon, Node 14 has an Intel macOS distribution, so use it under Rosetta. This project's Cypress 4 also predates native Apple Silicon support. If Cypress installation fails and you only need to run the app, skip its binary download:

```sh
CYPRESS_INSTALL_BINARY=0 npm ci
```

In Windows PowerShell, set `$env:CYPRESS_INSTALL_BINARY = '0'` before `npm ci` instead. This skips the Cypress executable, so end-to-end tests require a compatible Cypress installation separately.

The frontend connects to `http://localhost:8080` through `src/environments/config.ts`. Start the backend with the `dev` profile as described in the [backend README](../backend/README.md), then open the frontend using `http://localhost:4200`.

If you use a modern Node version and encounter `ERR_OSSL_EVP_UNSUPPORTED`, the legacy Webpack build needs `NODE_OPTIONS=--openssl-legacy-provider` for that command. Newer npm versions may also need `--legacy-peer-deps` during installation. Prefer the pinned project version for a shared setup.

## Development

### Development server

Run `npm start` to start the web application. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

### Code scaffolding

Run `npm run ng -- generate component component-name` to generate a new component. You can also use `npm run ng -- generate directive|pipe|service|class|guard|interface|enum|module`.

### Deployment

Run `npm run build` to build the project, or `npm run build:prod` for a production build. The build artifacts will be stored in the `dist/` directory.

Configure the following headers in the web server:

- `content-security-policy: script-src 'self'; object-src 'none';`

### GitHub Pages and EcoIndex

The GitHub Pages workflow in `../.github/workflows/deploy-pages.yml` builds the
frontend with the base path `/CardWiki---SSE-Version/`. It copies the built
`index.html` into a directory for each path listed in the workflow's `routes`
array, so direct requests to these routes return HTTP 200. Copying only to
`404.html` lets Angular render the page but keeps the HTTP 404 response, which
EcoIndex rejects.

After deployment, use URLs with a trailing slash, for example:

```text
https://vincenzo2002.github.io/CardWiki---SSE-Version/about/
https://vincenzo2002.github.io/CardWiki---SSE-Version/search/
https://vincenzo2002.github.io/CardWiki---SSE-Version/help/markdown-syntax/
```

Check a published route before submitting it to EcoIndex:

```sh
curl -sS -L -o /dev/null -w 'HTTP: %{http_code}\nType: %{content_type}\n' \
  https://vincenzo2002.github.io/CardWiki---SSE-Version/about/
```

The final response should be HTTP 200 with a `text/html` content type. Angular
still renders each page in the browser; these copies are not prerendered HTML.
For a dynamic route, add its concrete path (such as `decks/123` for an existing
deck) to the `routes` array and redeploy. A parameter pattern such as `decks/:id`
does not create pages for every ID.

This workflow deploys only the frontend. Pages that fetch data still need a
public backend configured in the production build; the default configuration
points to `http://localhost:8080`. Without a public backend, HTTP 200 confirms
that the page is served, but does not mean its data loaded successfully.
EcoIndex also starts without a logged-in session, so protected routes show the
login page instead of authenticated content.

### Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
