import React, { useCallback, useEffect } from "react";

const OAuthPopup = ({ url, onSuccess }) => {

	useEffect(() => {

		const width = 600;
		const height = 800;
		const left = (window.screen.width - width) / 2;
		const top = (window.screen.height - height) / 2;
		const options = `width=${width}, height=${height}, left=${left}, top=${top}, status=no, menubar=no, toolbar=no`;

		const popup = window.open(url, 'OAuth2Login', options);
		if (!popup) {
            console.error("팝업 차단이 활성화되어 있을 수 있습니다. 팝업 차단을 해제해주세요.");
            return; // 팝업 생성 실패 시 여기서 처리 종료
        }

		const handlePostMessage = event => {
			if (event.origin !== window.location.origin) return; // Ensure message is from our own window
			if (event.data === 'login-success') {
				popup.close();
				onSuccess();
			}
		};

		window.addEventListener('message', handlePostMessage);

		return () => {
			window.removeEventListener('message', handlePostMessage);
			if (!popup.closed) popup.close();
		};
		// 팝업 창 열기
		// const popup = window.open(
		// 	url,
		// 	`OAuthLogin`,
		// 	`width=${width}, height=${height}, top=${top}, left=${left}`
		// );

		// if(!popup) {
		// 	return;
		// }

		// // 팝업 메시지 수신
		// const onMessage = (e:MessageEvent):void => {

		// 	if (e.origin !== window.location.origin) return; // 보안 체크

		// 	if (e.data === `oauth-success`) {
		// 		onSuccess(`로그인 성공!`);
		// 		popup.close();
		// 		window.removeEventListener(`message`, onMessage);
		// 	}
		// }

		// window.addEventListener(`message`, onMessage);
		// return () => {
		// 	window.removeEventListener(`message`, onMessage);
		// };

	}, [url, onSuccess]);

	return null;
}

export default React.memo(OAuthPopup);