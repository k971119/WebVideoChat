let myStream;
let mute = false;
let cameraOff = false;

/*서버*/
//웹소켓 핸드쉐이크
var socket = new WebSocket('wss://192.168.0.46:443/socket/'+roomCode);
// STUN서버 등록 및 RTC 객체생성
var configuration = {
    "iceServers" : [{
        "url" : "stun:stun.l.google.com:19302"
    }]
}
var myPeerConnection = new RTCPeerConnection(configuration);
/*서버*/

/*이벤트*/
// 캔디데이트(나를 연결하는 방법들의 후보)를 등록(로컬디스크립션을 설정) 이벤트
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

// 소켓 이벤트
socket.onopen = function() {
    console.log("소켓 연결");
    getMediaNCreateOffer();
};
socket.onclose = (e) =>{
    console.log('소켓 닫힘');
}
socket.onerror = function (e){
    console.log('소켓 연결 에러');
}
socket.onmessage = async function(msg) {
    var content = JSON.parse(msg.data);
    if (content.event == "offer") {
        console.log("오퍼 수신");
        //오퍼가 올경우 REMOTE 등록
        var offer = content.data;
        myPeerConnection.setRemoteDescription(offer);

        //오퍼를 받고 자신의 미디어를 열어준다.
        await getMedia();
        myStream.getTracks().forEach((track) => myPeerConnection.addTrack(track, myStream));

        //미디어 데이터를 담아 ANSWER 전송
        var answer = await myPeerConnection.createAnswer();
        myPeerConnection.setLocalDescription(answer);
        console.log("ANSWER 전송");
        send({
            event: "answer",
            data: answer
        })
    } else if (content.event == "answer") {
        console.log("ANSWER RECEIVE");
        answer = content.data;
        myPeerConnection.setRemoteDescription(answer);

    //candidate요청
    } else if (content.event == "candidate") {
        console.log("ICECANDIDATE RECEIVE");

        //상대 클라이언트에 ICECANDIDATE 전송
        myPeerConnection.addIceCandidate(content.data);

    //RTC close 요청
    }else if(content.event == "closed"){
        //상대측 통화종료
        //채팅방 나가기
        location.href = "/";
    }
}
/*이벤트*/

/**
 * 시그널링 서버 전송용 함수
 * @param message
 */
function send(message) {
    socket.send(JSON.stringify(message));
}

/**
 * 미디어를 가져와서 VIDEO에 띄운후 전역 STREAM에 담는다
 * @returns {Promise<unknown>}
 */
async function getMedia(cameraId, audioId) {

    const userSelectInput = {
        audio : {deviceId:{exact:audioId}},
        video : {deviceId:{exact:cameraId}}
    }
    return new Promise(async (resolve, reject) => {
        try {
            await navigator.mediaDevices.getUserMedia(cameraId || audioId? userSelectInput:{
                video: true,
                audio: true
            }).catch(error => {
                console.log('미디오를 가져오는중 에러발생');
            }).then(async stream => {
                if (stream != undefined) {
                    myFace.srcObject = stream;
                    myStream = stream;
                } else {                //캠이없어서 stream못 읽어왔을 때 오디오만 받아서 비디오는 더미로 만들어 스트림으로 넘긴다
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
}

async function getDevices(){
    try{
        const devices = await navigator.mediaDevices.enumerateDevices();
        const videoInput = devices.filter(device => device.kind === "videoinput");
        const audioInput = devices.filter(device => device.kind === "audioinput");

        videoInput.forEach(camera => {
            const option = document.createElement("option");
            option.value = camera.deviceId;
            option.innerText = camera.label;
            cameraList.appendChild(option);
        });
        audioInput.forEach(audio => {
            const option = document.createElement("option");
            option.value = audio.deviceId;
            option.innerText = audio.label;
            audioList.appendChild(option);
        });
    }catch (e){
        console.log(e);
    }
}

// 더미 비디오 트랙을 생성하는 함수
function getDummyVideoTrack() {
    // 캔버스를 생성하여 더미 이미지를 그립니다.
    const canvas = document.createElement('canvas');
    canvas.width = 1280;
    canvas.height = 720;
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = 'gray';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // 캔버스의 내용을 기반으로 더미 비디오 스트림을 생성
    const dummyStream = canvas.captureStream(60);
    // 더미 비디오 트랙을 반환
    return dummyStream.getVideoTracks()[0];
}

// 연결실행
async function getMediaNCreateOffer() {

    await getMedia();
    // getMedia에서 가져온 audio, video 트랙을 myPeerConnection에 등록해요
    myStream.getTracks().forEach((track) => myPeerConnection.addTrack(track, myStream));

    createOffer();
}

async function createOffer(){     //현재 트랙 상태값에대한 값 송신
    // RTC객체에 미디어 담아서 offer 생성
    var offer = await myPeerConnection.createOffer();

    // offer 전송
    console.log("오퍼전송");
    await send({
        event: "offer",
        data: offer
    })

    // offer 로컬디스크립션 등록(등록시 onicecandidate 이벤트 호출)
    myPeerConnection.setLocalDescription(offer);
}

function endCall() {
    /*showToast("통화 종료");
    myPeerConnection.close();
    */
    send({event:'closed'});

    //socket.close();
    location.href = "/";
}