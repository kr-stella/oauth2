import React, {useCallback, useState} from "react";
import OAuthPopup from "./OAuthPopup";
import {useNavigate} from "react-router";

const LoginBtn = () => {
	const navigate = useNavigate();
	const [popupUrl, setPopupUrl] = useState<string | null>(null);

	const urls = [
		{ name: `Google`, url: `/oauth2/authorization/google` },
		{ name: `Kakao`,  url: `/oauth2/authorization/kakao` },
		{ name: `GitHub`, url: `/oauth2/authorization/github` },
		{ name: `Naver`,  url: `/oauth2/authorization/naver` },
	];

	const onSuccess = useCallback((v:string) => {
		alert(`${v} 로그인 성공!`);
		navigate(`/home`);
	}, [navigate]);

	return (
		<div>
			{urls.map((v) => (
				<button
					key={v.name}
					onClick={() => setPopupUrl(v.url)}
					style={{margin: `0 4px`}}
				>
					Login with {v.name}
				</button>
			))}

			{popupUrl && (
				<OAuthPopup
					url={popupUrl} // 선택한 OAuth 제공자의 URL
					onSuccess={() => onSuccess(popupUrl.split("/").pop()!)}
				/>
			)}
		</div>
		// <div>
		// 	<button onClick={() => setPopup(true)}>Login with GitHub</button>
		// 	{popup && (
		// 		<OAuthPopup
		// 			url={urls} // Spring Security OAuth2 경로
		// 			onSuccess={onSuccess}
		// 		/>
		// 	)}
		// </div>
	)
}

export default React.memo(LoginBtn);