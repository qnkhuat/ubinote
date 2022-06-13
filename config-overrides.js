const path = require('path');

module.exports = {
  // during dev write the build to disk so that BE can serves it
  devServer: function(configFunction) {
    return function(proxy, allowedHost) {
      const config = configFunction(proxy, allowedHost);
      config.devMiddleware['writeToDisk'] = true;
      return config;
    }
  },
  paths: function (paths, _env) {
    paths.appIndexJs = path.resolve(__dirname, 'src/frontend/index.tsx');
    paths.appSrc = path.resolve(__dirname, 'src/frontend');
    // build into the resources folder of java code app
    paths.appBuild = path.join(__dirname, 'resources/frontend');
    return paths;
  },
}
