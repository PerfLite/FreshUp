<div align="center">

<img src="assets/banner.svg" alt="FreshUp" width="100%">

<p>
  <img src="https://img.shields.io/github/v/release/PerfLite/FreshUp?style=flat-square" alt="release">
  <img src="https://img.shields.io/github/license/PerfLite/FreshUp?style=flat-square" alt="license">
  <img src="https://img.shields.io/github/languages/top/PerfLite/FreshUp?style=flat-square" alt="language">
  <img src="https://img.shields.io/github/stars/PerfLite/FreshUp?style=flat-square" alt="stars">
  <img src="https://img.shields.io/github/downloads/PerfLite/FreshUp/total?style=flat-square" alt="downloads">
</p>

<p>
  <a href="https://github.com/PerfLite/FreshUp/releases/latest"><b>⬇️ Скачать</b></a> ·
  <a href="https://github.com/PerfLite/FreshUp/releases">📦 Все релизы</a>
</p>

</div>

## ⬇️ Скачать

| Файл | Описание | Ссылка |
|---|---|---|
| `app-release.apk` | Основная сборка для Android (8.0+) | [Скачать](https://github.com/PerfLite/FreshUp/releases/latest/download/app-release.apk) |

> Старые версии и список изменений: [Релизы](https://github.com/PerfLite/FreshUp/releases)

---

## Возможности

- 📷 **Сканер штрихкодов** — мгновенное распознавание штрихкодов через камеру с помощью Google ML Kit и автозаполнением названий из базы Open Food Facts.
- 🔦 **Поддержка фонарика** — удобное включение подсветки камеры для сканирования штрихкодов в темноте или в холодильнике.
- 🧊 **Зоны хранения** — разделение продуктов по местам хранения: «Холодильник», «Морозилка», «Шкаф».
- ⚡ **Быстрые пресеты срока** — выбор срока годности в одно касание (`+1 дн`, `+3 дн`, `+1 нед`, `+2 нед`, `+1 мес`, `+6 мес`).
- 📊 **Интерактивный дашборд** — компактные плитки со счётчиками срочности («Срочно», «Скоро», «В норме»), работающие как фильтры.
- 🔍 **Мгновенный поиск** — поиск и фильтрация продуктов по названию в реальном времени.
- ♻️ **Трекер Zero Waste** — кнопки «Съедено» и «Выброшено» на карточках, круговая статистика и советы по снижению пищевых отходов.
- 🔔 **Умные уведомления** — своевременные push-напоминания о продуктах до истечения их срока годности (AlarmManager + WorkManager).
- 🌙 **Тёмная тема** — глубокий комфортный тёмный интерфейс Material 3.

## Установка

1. Скачайте файл `app-release.apk` из раздела [Релизы](https://github.com/PerfLite/FreshUp/releases/latest).
2. При необходимости разрешите установку приложений из неизвестных источников в настройках устройства.
3. Установите и запустите приложение.

## Сборка

```bash
git clone https://github.com/PerfLite/FreshUp.git
cd FreshUp
./gradlew assembleRelease
```

Собранный APK будет находиться в папке `apk/app-release.apk` (или `app/build/outputs/apk/release/app-release.apk`).

## Лицензия

GPL-3.0 — см. [LICENSE](LICENSE).

<p align="right"><a href="#">↑ Наверх</a></p>
