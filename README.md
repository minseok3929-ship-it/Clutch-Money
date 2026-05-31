# ClutchRPG

Minecraft Paper 1.21.11 / Java 21 / Gradle 기준의 울창한 숲 RPG 전투 MVP 프로토타입 플러그인입니다.

## 목표

완성형 MMORPG가 아니라 전투 손맛, 빠른 ARPG 사냥, MMORPG식 보스 패턴, 성장 체감, 희귀 드랍 연출을 빠르게 검증하기 위한 단일 jar 플러그인입니다.

## 주요 기능

- `/스텟` GUI 기반 스텟 성장과 투자 효과 미리보기
- STR/DEX/INT/VIT/LUK, 소프트캡, 레벨 50 성장 구조
- 검 3타 콤보: 기본 베기, 올려치기, 내려찍기 충격파
- 활 에너지 화살: 초록색 중심 파티클 궤적과 적중 폭발
- 스태프 구체형 마법탄: 붉은색/보라색 파티클 투사체와 범위 표시
- 점프 중 Shift 대쉬: 3초 쿨타임과 잔상 파티클
- PersistentDataContainer 기반 장비 데이터와 한글 옵션 로어
- RARE/EPIC 드랍 액션바, 타이틀, 사운드, 파티클 연출
- 자연 스폰 바닐라 몬스터를 차단하는 CustomMob/adapter 체계와 울창한 숲 몬스터 4종의 체력 표시, 특수 행동, 사망 연출
- 스폰 포인트 기반 몬스터 캠프/필드형 그룹 스폰(maxAlive/radius/batch), config 기반 숲 슬라임 체력 조정
- BossBar와 선경고 장판을 갖춘 보스 `숲의 수호자`
- SQLite 플레이어 데이터 저장

## 관리자 명령어

- `/crpg reload`
- `/crpg mob spawn <mobId>`
- `/crpg boss spawn forest_guardian`
- `/crpg item give <weaponType> <rarity>`
- `/crpg spawnpoint add <mobId> <maxAlive> <radius>`
- `/crpg spawnpoint list`
- `/crpg spawnpoint remove <id>`

## 빌드

```bash
gradle build --no-daemon
```

빌드 결과물은 `build/libs/ClutchRPG-0.1.0.jar` 입니다.
