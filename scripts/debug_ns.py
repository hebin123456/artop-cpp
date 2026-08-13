#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""调试: 检查 eStructuralFeatures 的实际命名空间"""
from lxml import etree

for f, label in [("/tmp/official_autosar448.ecore", "OFFICIAL"),
                 ("/workspace/decompiler/autosar448/model/dummy.ecore", "DUMMY")]:
    print("=" * 60)
    print(label, f)
    print("=" * 60)
    cnt = 0
    for event, elem in etree.iterparse(f, events=("end",)):
        tag = elem.tag
        if "eStructuralFeatures" in tag:
            cnt += 1
            if cnt <= 3:
                print("  tag repr:", repr(tag))
                print("  xsi:type:", elem.get("{http://www.w3.org/2001/XMLSchema-instance}type"))
                print("  eType:", elem.get("eType"))
            if cnt >= 5:
                break
    print("  (stopped after first few) total found in this run:", cnt)
    print()
