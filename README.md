# ClutchRPG

Minecraft Paper 1.21.11 / Java 21 / Gradle 기준의 울창한 숲 RPG 전투 MVP 프로토타입 플러그인입니다.

## 목표

완성형 MMORPG가 아니라 전투 손맛, 빠른 ARPG 사냥, MMORPG식 보스 패턴, 성장 체감, 희귀 드랍 연출을 빠르게 검증하기 위한 단일 jar 플러그인입니다.

## 주요 기능

- `/스텟` GUI 기반 스텟 성장
- STR/DEX/INT/VIT/LUK, 소프트캡, 레벨 50 성장 구조
- 검 3타 콤보, 빠른 활 발사, 스태프 마법탄 폭발
- 점프 중 Shift 대쉬, 포션 쿨타임
- PersistentDataContainer 기반 장비 데이터와 COMMON/RARE/EPIC 실드랍
- 울창한 숲 몬스터 4종과 보스 `숲의 수호자`
- SQLite 플레이어 데이터 저장

## 빌드

```bash
gradle build --no-daemon
```

빌드 결과물은 `build/libs/ClutchRPG-0.1.0.jar` 입니다.
