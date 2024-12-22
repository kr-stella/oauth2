import React, {Fragment, useCallback, useState} from "react";
import OAuthPopup from "./OAuthPopup";
import {useNavigate} from "react-router";

const LoginBtn = () => {
	const navigate = useNavigate();
	const [popupUrl, setPopupUrl] = useState<string | null>(null);

	const urls = [
		{ name: `google`, url: `/oauth2/authorization/google` },
		{ name: `kakao`,  url: `/oauth2/authorization/kakao` },
		{ name: `github`, url: `/oauth2/authorization/github` },
		{ name: `naver`,  url: `/oauth2/authorization/naver` },
	];

	const onSuccess = useCallback(() => {
		navigate(`/home`);
	}, [navigate]);

	return (
		<div style={{ marginTop: `16px`, display:`flex`, flexFlow:`row-wrap`, alignItems:`center`, justifyContent: `space-evenly`}}>
			{urls.map((v, i) => (
				<div key={i} style={{ display:`flex`, flexFlow:`column`, alignItems:`center`, justifyContent:`center` }}>
					<button
						key={v.name}
						onClick={() => setPopupUrl(v.url)}
						style={{margin: `0 4px`, backgroundColor:`transparent`}}
					>
						<img src={`/resources/image/${v.name}.png`} alt={v.name} width={`48px`} height={`48px`} />
					</button>
					<span>{v.name}</span>
				</div>
			))}
			{popupUrl && (
				<OAuthPopup
					url={popupUrl} // 선택한 OAuth 제공자의 URL
					onSuccess={() => onSuccess()}
				/>
			)}
		</div>
	)
}

export default React.memo(LoginBtn);