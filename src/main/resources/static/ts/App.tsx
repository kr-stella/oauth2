import "react-app-polyfill/ie11";
import "react-app-polyfill/stable";
import React from "react";
import * as ReactDOM from "react-dom/client";

import LoginBtn from "./component/view/LoginBtn";
import {Route, Routes} from "react-router";
import Home from "./component/view/Home";
import {BrowserRouter} from "react-router-dom";

const root = ReactDOM.createRoot(document.getElementById(`root`) as HTMLElement);
root.render(
	<BrowserRouter>
		<Routes>
			<Route path={`/`} element={<LoginBtn />} />
			<Route path={`/home`} element={<Home />} />
		</Routes>
	</BrowserRouter>
	// <LoginBtn />
);