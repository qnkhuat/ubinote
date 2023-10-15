const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const webpack = require('webpack');
const path = require('path');
const autoProcesss = require('svelte-preprocess');

const mode = process.env.NODE_ENV || 'development';
const prod = mode === 'production';

module.exports = {
	entry: {
		'build/bundle': ['./src/frontend/main.js']
	},
	resolve: {
		alias: {
			svelte: path.dirname(require.resolve('svelte/package.json')),
			frontend: path.resolve(__dirname, 'src/frontend/')
		},
		extensions: ['.mjs', '.js', '.svelte'],
		mainFields: ['svelte', 'browser', 'module', 'main']
	},
	output: {
		path: path.join(__dirname, 'resources/frontend/'),
		filename: '[name].js',
		chunkFilename: '[name].[id].js'
	},
	module: {
		rules: [
			{
				test: /\.(html|svelte)$/,
				use: {
					loader: 'svelte-loader',
					options: {
						preprocess: autoProcesss(),
						compilerOptions: {
							dev: !prod
						},
						emitCss: true,
						hotReload: !prod,
						onwarn: (warning, _handler) => {
							const { code } = warning;
							//const skip_lint = ["css-unused-selector"]
							const skip_lint = [];
							if (skip_lint.includes(code))
								return;
						}
					}
				},
			},
			{
				// a regex that matches css sass scss
				test: /\.(css|sass|scss)$/,
				use: [
					MiniCssExtractPlugin.loader,
					'css-loader',
					"sass-loader",
				]
			},
			{
				// required to prevent errors from Svelte on Webpack 5+
				test: /node_modules\/svelte\/.*\.mjs$/,
				resolve: {
					fullySpecified: false
				}
			}
		]
	},
	mode,
	plugins: [
		new MiniCssExtractPlugin({
			filename: '[name].css'
		}),
		new webpack.DefinePlugin({
			// parse all the env vars and replace them with their values
			// so you can use things like process.env.NODE_ENV in the code
			'process.env': Object.keys(process.env).reduce((acc, key) => {
				acc[key] = JSON.stringify(process.env[key]);
				return acc;
			}, {})
		})

	],
	devtool: prod ? false : 'inline-source-map',
	devServer: {
		static: "resources/frontend",
		hot: true,
		devMiddleware: {
			writeToDisk: true,
		},
	},
};
