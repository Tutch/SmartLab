# SmartLab

O SmartLab é um sistema em desenvolvimento que permite o gerenciamento e coleta de informações em laboratórios de informática, com o objetivo de reduzir o consumo de energia desligando os computadores quando são julgados como ociosos pelo sistema.

A arquitetura do SmartLab consiste de 3 partes principais:

- Agente, que funciona nas máquinas monitoradas do laboratório.
- Coordenador, que gerencia as máquinas que estão no laboratório e toma decisões de desligamento através de informações que coleta do ambiente através dos dados coletados por uma placa Arduino,
- Serviços Web, que servem como ponto de acesso para o servidor por possíveis cliente. Nessa versão, um cliente web responsivo fornece as informações dos laboratórios que estão no coordenador e permite o desligamento das máquinas.
