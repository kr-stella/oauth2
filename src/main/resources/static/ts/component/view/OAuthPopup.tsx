import React, { useCallback, useEffect } from "react";

const OAuthPopup = ({ url, onSuccess }) => {

	useEffect(() => {
		const width = 600;
		const height = 700;
		const left = (window.screen.width - width) / 2;
		const top = (window.screen.height - height) / 2;

		// 팝업 창 열기
		const popup = window.open(
			url,
			`OAuthLogin`,
			`width=${width}, height=${height}, top=${top}, left=${left}`
		);

		if(!popup) {
			return;
		}

		// 팝업 메시지 수신
		const onMessage = (e:MessageEvent):void => {

			if (e.origin !== window.location.origin) return; // 보안 체크

			if (e.data === `oauth-success`) {
				onSuccess(`로그인 성공!`);
				popup.close();
				window.removeEventListener(`message`, onMessage);
			}
		}

		window.addEventListener(`message`, onMessage);
		return () => {
			window.removeEventListener(`message`, onMessage);
		};

	}, [url, onSuccess]);

	return null;
}

export default React.memo(OAuthPopup);