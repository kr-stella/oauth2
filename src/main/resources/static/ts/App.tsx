import "react-app-polyfill/ie11";
import "react-app-polyfill/stable";
import "../style/global.scss";
import "../style/main.scss";
/** FontAwesome */
import "@fortawesome/fontawesome-pro/css/all.min.css";

import React from "react";
import * as ReactDOM from "react-dom/client";

import { Route, Routes } from "react-router";
import { BrowserRouter } from "react-router-dom";

import Main from "./component/view/Main";
import Home from "./component/view/Home";

const root = ReactDOM.createRoot(document.getElementById(`root`) as HTMLElement);
root.render(
	<BrowserRouter>
		<Routes>
			<Route path={`/`} element={<Main />} />
			<Route path={`/home`} element={<Home />} />
		</Routes>
	</BrowserRouter>
);