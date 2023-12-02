let myStream;
/*setTimeout(() => {
    // 일단 소켓 연결을 해봐요
    socket = new WebSocket('wss://192.168.45.93:443/socket/'+roomCode);
})*/

var socket = new WebSocket('wss://192.168.45.93:443/socket/'+roomCode);
// 먼저 RTC객체를 만들어요 구글 stun 서버로 부터 나의 정보를 가져올게요
var configuration = {
    "iceServers" : [{
        "url" : "stun:stun.l.google.com:19302"
    }]
}
var myPeerConnection = new RTCPeerConnection(configuration);

/*이벤트*/
// 내가 나의 캔디데이트(너가 나를 연결하는 방법들의 후보)를 등록하면(즉 로컬디스크립션을 설정하면)
myPeerConnection.onicecandidate = event => {
    console.log("icecandidate 전송");
    send({
        event: "candidate",
        data: event.candidate
    })
}
// 피어에서 track을 받아올 때 호출
myPeerConnection.addEventListener("track", (event) =>{
    peerFace.srcObject = event.streams[0];
});
function handleAddStream(data) {
    console.log("스트리밍 데이터를 받아왔어요");
    var peerFace = document.getElementById("peerFace");
    peerFace.srcObject = data.stream;
}

// 소켓이 연결되었을 때 실행할 콜백함수에요
socket.onopen = function() {
    console.log("웹소켓이 연결되었어요");
};
socket.onclose = (e) =>{
    console.log('소켓 닫힘');
}
socket.onerror = function (e){
    console.log('소켓 연결 에러');
}

// 소켓에서 메세지를 받아왔을 때 실행할 콜백함수에요
socket.onmessage = async function(msg) {
    var content = JSON.parse(msg.data);
    if (content.event == "offer") {
        console.log("오퍼가 왔어요");
        // 오퍼가 오면 가장먼저 그 오퍼를 리모트 디스크립션으로 등록해줘요
        var offer = content.data;
        myPeerConnection.setRemoteDescription(offer);
        // 받는 쪽에서도 자신의 미디어를 켜줘요
        await getMedia();
        myStream.getTracks().forEach((track) => myPeerConnection.addTrack(track, myStream));
        // 이제 앤서를 보내요
        var answer = await myPeerConnection.createAnswer();
        myPeerConnection.setLocalDescription(answer);
        console.log("앤서를 보낼게요");
        send({
            event: "answer",
            data: answer
        })
    } else if (content.event == "answer") {
        console.log("앤서가 왔어요");
        answer = content.data;
        myPeerConnection.setRemoteDescription(answer);
    } else if (content.event == "candidate") {
        console.log("캔디데이트가 왔어요");
        // 이 메서드를 통해 리모트 디스크립션에 설정되어있는 피어와의 연결방식을 결정해요
        myPeerConnection.addIceCandidate(content.data);
    }
}
/*이벤트*/

// 앞으로 소켓으로 메세지를 보낼 땐 이 함수를 쓸 생각이에요
function send(message) {
    socket.send(JSON.stringify(message));
}

//미디어 내용을 받기 시작하는 함수에요
//미디어와 오디오를 분리할 필요가 있어보임
async function getMedia() {
    return new Promise(async (resolve, reject) => {
        try {
            await navigator.mediaDevices.getUserMedia({
                video: true,
                audio: true
            }).catch(error => {
                console.log('미디오를 가져오는중 에러발생');
            }).then(async stream => {
                if (stream != undefined) {           //에러나서 stream못 읽어왔을때 오디오만 받아서 비디오는 더미 이미지 스트림으로 넘긴다
                    myFace.srcObject = stream;
                    myStream = stream;
                } else {
                    await navigator.mediaDevices.getUserMedia({audio: true})
                        .then(stream => {
                            // 오디오 스트림에 더미 비디오 트랙을 추가합니다.
                            const dummyVideoTrack = getDummyVideoTrack();
                            stream.addTrack(dummyVideoTrack);
                            // 수정된 스트림을 반환
                            myFace.srcObject = stream;
                            myStream = stream;
                        });
                }
            });

            resolve();
        } catch (e) {
            console.log("비디오를 가져오는 중 에러 발생");
            reject(e);
        }
    });

    // 더미 비디오 트랙을 생성하는 함수
    function getDummyVideoTrack() {
        // 캔버스를 생성하여 더미 이미지를 그립니다.
        const canvas = document.createElement('canvas');
        // canvas.width = 1280;
        // canvas.height = 720;
        const ctx = canvas.getContext('2d');
        ctx.fillStyle = 'gray';
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        // 캔버스의 내용을 기반으로 더미 비디오 스트림을 생성합니다.
        const dummyStream = canvas.captureStream(60);
        // 더미 비디오 트랙을 반환합니다.
        return dummyStream.getVideoTracks()[0];
    }
}

// '오퍼를 생성해요'라는 버튼을 눌렀을 때 이 메서드가 실행되요
async function createOffer() {

    await getMedia();
    // getMedia에서 가져온 audio, video 트랙을 myPeerConnection에 등록해요
    myStream.getTracks().forEach((track) => myPeerConnection.addTrack(track, myStream));

    // RTC객체에 미디어 담아서 offer 생성
    var offer = await myPeerConnection.createOffer();
    
    // offer 전송
    console.log("오퍼전송");
    await send({
        event: "offer",
        data: offer
    })
    
    //offer 로컬디스크립션 등록(등록시 onicecandidate 이벤트 호출)
    myPeerConnection.setLocalDescription(offer);
}