#!/bin/sh

trivy fs --scanners secret -q --exit-code 1 --skip-dirs .idea,.gradle,build .