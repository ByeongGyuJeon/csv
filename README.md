# CSV memory lab

첫 벤치마크는 UTF-8 CSV를 `BufferedReader.readLine()`으로 한 줄씩 읽어 행 수만 세는 기준선입니다. 매초 진행률, 처리 행 수, 처리 속도를 표시하고 마지막에 총 시간을 출력합니다.

## Local run

```powershell
javac -encoding UTF-8 -d out src/main/java/lab/RowByRowProgress.java
java -cp out lab.RowByRowProgress C:\data\input.csv
```

## 512 MiB container run

```powershell
docker build -t csv-memory-lab .
docker run --rm --memory=512m --memory-swap=512m --cpus=1 `
  -v "C:\data:/data:ro" csv-memory-lab /data/input.csv
```

`--memory=512m`은 JVM 힙 외의 메모리도 포함한 컨테이너 상한입니다. JVM 힙은 이미지의 `-Xmx384m`으로 제한해 네이티브 메모리 여유를 남깁니다.
