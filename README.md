test

```bash
mkdir repos
cd mkdir repos
```

```bash
cd /repos
git clone https://github.com/yhh1056/atdd-camping-reservation.git --branch yhh1056 --single-branch
git clone https://github.com/yhh1056/atdd-camping-admin.git --branch yhh1056 --single-branch
git clone https://github.com/yhh1056/atdd-camping-kiosk.git --branch main --single-branch
```

DB 컨테이너 실행
```bash
docker compose up -d
```

애플리케이션 실행
```bash
- docker compose up -d
```
