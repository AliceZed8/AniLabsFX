# AniLabsFX
AniLabsFX — это приложение для поиска и просмотра аниме, использующее API AniLabs. 
Написано с использованием библиотеки JavaFX.

## Требования
### GStreamer
Используется для проигрывания MP4 HLS.
#### Windows
- Скачать можно [здесь](https://gstreamer.freedesktop.org/download/#linux)
- Также установщик есть в папке **/gstreamer/win/**
#### Linux
##### Arch
```
sudo pacman -S gstreamer gst-libav gst-plugins-base gst-plugins-good gst-plugins-bad gst-plugins-ugly
```
## Сборка и запуск
Для сборки используется Gradle.
### Windows
```
gradlew.bat build
gradlew.bat run
```
### Linux
```
./gradlew build
./gradlew run
```
