# Java Network-Based Features

Readme also available on [russian](readme_ru.md).

## About:
This project is a collection of features that are based on the Pcap4j.
Developed as a lab project for the course [SSfLCN](http://bsuir-helper.ru/predmet/spolks)
(System software for local computer networks). [BSUIR](https://www.bsuir.by/) 8th semester.

## Features:
* Ping
* Traceroute
* Broadcast chat
* Multicast chat

## Pre requirements:

### Common:
* JDK 17

### Windows:
* WinPcap compatible driver (recommended [Npcap](https://npcap.com/))

### Linux:
There is no prerequisites for Linux. (Tested on Ubuntu 20.04)

## Properties description:
* app.task - task of the application (ping, traceroute, broadcast_chat, multicast_chat)
* app.device-address - IPv4 address of the network device (if not specified, application will ask for it on start)
* app.next-hop-mac-address - MAC address of the next hop device
* app.chat-broadcast-address - Broadcast address of the network

Other properties may not be changed.