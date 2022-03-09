# Java Network-Based Features

Readme also available on [english](readme.md).

## Описание
Это приложение это набор функций, написанных с использованием [Pcap4j](https://github.com/kaitoy/pcap4j).
Разработано в качестве лабораторной работы по [СПОЛКС](http://bsuir-helper.ru/predmet/spolks)
(Системное программное обеспечение локальных компьютерных сетей). [BSUIR](https://www.bsuir.by/) 8th semester.

## Функции:
* Ping
* Traceroute
* Broadcast chat
* Multicast chat

## Условия работы:

### Общие:
* JDK 17

### Windows:
* WinPcap совместимый драйвер (рекомендуется [Npcap](https://npcap.com/))

### Linux:
Нету. (Протестировано на Ubuntu 20.04)

## Описание параметров:
* app.task - функция приложения (ping, traceroute, broadcast_chat, multicast_chat)
* app.device-address - IPv4 сетевого интерфейса (если не указано, приложение запросит его при старте)
* app.next-hop-mac-address - MAC адрес следующего девайса (next hop)
* app.chat-broadcast-address - Широковещательный адрес сети

Остальные параметры можно не менять.