# ClutchRPG

Minecraft Paper 1.21.11, Java 21, Gradle 기준으로 제작한 **CLUTCH RPG PROJECT 1차 전투 프로토타입**입니다.
목표는 전체 RPG 서버 완성이 아니라, 울창한 숲 Lv.1~10 구간에서 빠른 ARPG 사냥감과 MMORPG식 보스 패턴을 테스트하는 것입니다.

## 포함된 프로토타입 범위

- 플레이어 레벨/경험치/스텟/스텟 포인트 시스템
- STR, DEX, INT, VIT, LUK 및 소프트캡 기반 스케일링
- 검/활/스태프 우클릭 무기 스킬과 해머/방패 확장 슬롯
- Q/R/F 스텟 스킬 슬롯 확장용 enum 구조
- 마나와 글로벌 쿨타임 없는 개별 쿨타임 전투
- 점프 후 Shift 대쉬와 장비 옵션 기반 대쉬 쿨타임 감소 구조
- 치명타, 방어 관통 확장값, 상태이상 구조
- PDC 기반 커스텀 장비, 희귀도, 옵션 풀, 요구 스텟
- 울창한 숲 일반 몬스터 4종과 숲의 수호자 보스
- SQLite 플레이어 데이터 저장

## 관리자 테스트 명령어

```text
/crpg stats
/crpg stat add <stat> <amount>
/crpg level set <level>
/crpg item give <weaponType> <rarity>
/crpg mob spawn <mobId>
/crpg boss spawn forest_guardian
/crpg reload
```

예시:

```text
/crpg stat add STR 20
/crpg item give sword epic
/crpg mob spawn forest_slime
/crpg boss spawn forest_guardian
```

## 빌드

```bash
gradle build
```

네트워크가 허용된 환경에서는 Paper API와 SQLite JDBC 의존성을 받아 `build/libs/ClutchRPG-0.1.0-prototype.jar`를 생성합니다.
