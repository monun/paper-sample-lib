# Paper multi project (1.18+)

[![Kotlin](https://img.shields.io/badge/java-17-ED8B00.svg?logo=java)](https://www.azul.com/)
[![Kotlin](https://img.shields.io/badge/kotlin-1.6.10-585DEF.svg?logo=kotlin)](http://kotlinlang.org)
[![Gradle](https://img.shields.io/badge/gradle-7.3.3-02303A.svg?logo=gradle)](https://gradle.org)
[![GitHub](https://img.shields.io/github/license/monun/paper-sample-lib-nms)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Kotlin](https://img.shields.io/badge/youtube-각별-red.svg?logo=youtube)](https://www.youtube.com/channel/UCDrAR1OWC2MD4s0JLetN0MA)

---

### 프로젝트 구성

1. 프로젝트 이름 변경
    * `settings.gradle.kts / rootProject.name=sample`
2. 서브프로젝트 구성
    * `./gradlew setupModules`

---

### 의존성 가져오기 `net.minecraft.server`

1. core 프로젝트 하위에 버전 이름의 프로젝트 생성
    * `:core:v1.18`
    * `:core:v1.18.1`
2. 태스크 실행
    * `./gradlew setupDependencies`

---

### NOTE

* 버전별 의존성 태스크 `setup<Module><Version>`
  * `seupPaper1.18`
  * `seupSpigotAll`
  * `seupSpigot1.18`
  * `seupSpigotAll`
