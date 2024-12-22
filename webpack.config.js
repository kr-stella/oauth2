// WebpackManifestPlugin >> WEBPACK 5 내장으로 삭제
const path = require(`path`);
const fs = require(`fs`);
const webpack = require(`webpack`);

// const autoprefixer = require(`autoprefixer`);	// 설정방법따라 다름
const MiniCssExtractPlugin = require(`mini-css-extract-plugin`);

const HtmlWebpackPlugin = require(`html-webpack-plugin`);
const ForkTsCheckerWebpackPlugin = require(`fork-ts-checker-webpack-plugin`);
const TerserPlugin = require(`terser-webpack-plugin`);

const { CleanWebpackPlugin } = require(`clean-webpack-plugin`);
const { BundleAnalyzerPlugin } = require(`webpack-bundle-analyzer`);

const src = path.join(__dirname, `src`);
const modules = path.join(__dirname, `node_modules`);
if(fs.existsSync(src) && fs.existsSync(modules)) {
	module.exports = (_, args) => {

		const mode = args.mode;
		const dev = mode === `development`;
		const prod = mode === `production`;

		const data = {
			stats: {
				colors: true, modules: true,
				reasons: true, errorDetails: true,
				assets: true, chunks: true,
				chunkModules: true, chunkOrigins: true,
				usedExports: true, providedExports: true
			},
			optimization: {
				splitChunks: {
					cacheGroups: {
						vendors: {
							test: /[\\/]node_modules[\\/]/,
							name: `vendors`,
							chunks: `all`
						}
					}
				}
			},
			entry: {
				app: path.join(__dirname, `src`, `main`, `resources`, `static`, `ts`, `App.tsx`),
			},
			resolve: {
				symlinks: false,
				extensions: [`.tsx`, `.ts`, `.js`, `.scss`]
			},
			output: {
				path: path.resolve(__dirname, `src`, `main`, `resources`, `static`, `built`),
				publicPath: `/resources/built/`,
				filename: `[name].[chunkhash].js`,
				chunkFilename: `[name].[chunkhash].js`,
				assetModuleFilename: `[hash][ext][query]`,
				clean: true,
			},
			plugins: [
				new MiniCssExtractPlugin({
					filename: `[name].[chunkhash].css`
				}),
				new webpack.ProvidePlugin({
					"window.Quill": `quill`
				}),
				new CleanWebpackPlugin(),
				new HtmlWebpackPlugin({
					filename: path.resolve(__dirname, `src`, `main`, `resources`, `templates`, `index.html`),
					template: path.resolve(__dirname, `src`, `main`, `resources`, `templates`, `template.ejs`),
				})
				//{
				//	apply: (compiler) => {
				//		compiler.hooks.done.tap(`ManifestPlugin`, (stats) => {
				//			fs.writeFileSync(
				//				path.resolve(compiler.options.output.path, `manifest.json`),
				//				JSON.stringify(stats.toJson().assetsByChunkName)
				//			);
				//		});
				//	}
				//}
			],
			watchOptions: {
				/** 감시에서 제외 */
				ignored: [ `**/node_modules/**`, `**/build/**`, `**/bin/**` ]
				/** 변경 사항을 모은 후 재구축하기 전의 지연 시간 */
				// aggregateTimeout: 250
			},
			module: {
				rules: [
					{
						test: /\.(js|jsx)?$/,
						exclude: /node_modules(?!\/quill-image-drop-module|quill-image-resize-module)/,
						include: [src, path.join(__dirname, `sagaMonitor`)],
						use: {
							loader: `babel-loader`,
							options: {
								presets: [
									`@babel/preset-env`,
									`@babel/preset-react`
								],
								plugins: [
									`@babel/plugin-syntax-dynamic-import`,
									`@babel/plugin-proposal-class-properties`,
									`@babel/plugin-transform-runtime`
								]
							}
						}
					},
					{
						test: /\.(ts|tsx)$/,
						exclude: /node_modules/,
						use: [
							{
								loader: `babel-loader`,
								options: {
									presets: [
										`@babel/preset-env`,
										`@babel/preset-react`,
										`@babel/preset-typescript`
									],
									plugins: [
										`@babel/plugin-transform-runtime`
									]
								}
							},
							{
								loader: `ts-loader`,
								options: {
									// configFile,
									transpileOnly: true,
									experimentalWatchApi: true,
								}
							}
						]
					},
					/** 스타일 모듈 파일( 파일 이름이 .module.scss로 끝나는 경우 ) */
					{
						test: /\.module\.scss$/,
						/** node_modules 폴더 제외 */
						exclude: /node_modules/,
						use: [
							dev? `style-loader`:MiniCssExtractPlugin.loader,
							{
								loader: `css-loader`,
								options: {
									/** sass-loader, postcss-loader를 포함하므로 2로 설정 */
									importLoaders: 2,
									sourceMap: true,
									modules: {
										mode: `local`,
										localIdentName: `[local]-[hash:base64:4]`,
										exportGlobals: true,
										/** 캐멀케이스로 변환 ( main-class >>> mainClass ) */
										// exportLocalsConvention: `camelCase`,
										/** css파일로 클래스를 내보내지 않고 모듈로 내보내기 설정 */
										exportOnlyLocals: false,
									}
								}
							},
							{
								loader: `postcss-loader`,
								options: {
									postcssOptions: {
										plugins: [
											`postcss-flexbugs-fixes`,
											[`autoprefixer`, {
												overrideBrowserslist: [
													`> 0.2%`,
													`last 2 version`,
													`Firefox ESR`,
													`not ie < 9`,
													`iOS >= 8`,
													`Safari >= 8`,
													`Android > 4.4`
													// `last 4 version`
													// `not dead`
												],
												flexbox: `no-2009`
											}]
										]
									}
								}
							},
							`sass-loader`
						]
					},
					{
						test: /\.(sa|sc)ss$/i,
						exclude: [/node_modules/, /\.module\.scss$/],
						use: [
							MiniCssExtractPlugin.loader,
							{
								loader: `css-loader`,
								options: {
									importLoaders: 2
								}
							},
							{
								loader: `postcss-loader`,
								options: {
									postcssOptions: {
										plugins: [
											`postcss-flexbugs-fixes`,
											[`autoprefixer`, {
												overrideBrowserslist: [
													`> 0.2%`,
													`last 2 version`,
													`Firefox ESR`,
													`not ie < 9`,
													`iOS >= 8`,
													`Safari >= 8`,
													`Android > 4.4`
													// `last 4 version`
													// `not dead`
												],
												flexbox: `no-2009`
											}]
										]
									}
								}
							},
							{
								loader: `sass-loader`,
								options: {
									sassOptions: {
										includePaths: [
											path.join(__dirname, `src`, `main`, `resources`, `static`, `style`)
										],
										indentType: `tab`,
										indentWidth: 1,
										outputStyle: `expanded`
									}
								}
							}
						]
					},
					{
						test: /\.css$/,
						use: [ `style-loader`, `css-loader` ]
					},
					{
						test: /\.(ico|png|jpe?g|webp|gif|svg|woff|woff2|ttf|otf|eot)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
						type: `asset/resource`, // Webpack 5에 내장된 asset module을 사용
					}
				]
			}
		};

		if(dev) return {
			...data,
			devtool: `eval-source-map`,
			plugins: [
				...data.plugins,
				new BundleAnalyzerPlugin({
					analyzerPort: 58085
				}),
				new webpack.DefinePlugin({ // 컴파일할 코드에서 특정 문자열을 설정한 값으로 치환.
					"NODE_MODE": JSON.stringify(`development`),
					"process.env.NODE_ENV": JSON.stringify(`development`),
					"MAIN_HOST": JSON.stringify(`http://localhost:9000`)
				})
			]
		};
		else if(prod) return {
			...data,
			optimization: {
				minimize:true,
				minimizer: [new TerserPlugin()],
				...data.optimization
			},
			plugins: [
				...data.plugins,
				new webpack.DefinePlugin({ // 컴파일할 코드에서 특정 문자열을 설정한 값으로 치환.
					"NODE_MODE": JSON.stringify(`production`),
					"process.env.NODE_ENV": JSON.stringify(`production`),
					"API_HOST": JSON.stringify(`https://api.intra.captivision.co.kr`),
					"MAIN_HOST": JSON.stringify(`https://as.intra.captivision.co.kr`)
				})
			]
		}
		else return {
			...data,
			optimization: {
				minimize:true,
				minimizer: [new TerserPlugin()],
				...data.optimization
			},
			plugins: [
				...data.plugins,
				new webpack.DefinePlugin({ // 컴파일할 코드에서 특정 문자열을 설정한 값으로 치환.
					"NODE_MODE": JSON.stringify(`deployment`),
					"process.env.NODE_ENV": JSON.stringify(`production`),
					"API_HOST": JSON.stringify(`https://api.dev.captivision.co.kr`),
					"MAIN_HOST": JSON.stringify(`https://as.dev.captivision.co.kr`)
				})
			]
		};

	};
}