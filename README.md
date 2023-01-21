# jenkins, docker, nginx one click install

gcp나 aws를 이용한 CI/CD 자동화 환경 구성 중 필요한 것들을 매번 설치하기 귀찮아서 만든 실행파일 입니다.

순서는 jenkins, docker, nginx 순으로 설치되고 -y 옵션을 줘서 기다리기만 하면 됩니다.

## 실행 방법

```shell
sh jenkins-docker.sh
```

vm에서 이 레포를 clone 한 다음 폴더로 들어가서 위 명령어를 치기만 하면 된다.

## 주의

docker는 권한 666을 사용했기 때문에 참고하기 바랍니다.
