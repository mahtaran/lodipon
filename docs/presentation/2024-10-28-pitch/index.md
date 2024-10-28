---
marp: true

headingDivider: 4
lang: en-GB

theme: uncover
style: |
  /* Colours */
  :root {
    --color-header: rgba(32, 34, 40, .8);
  }

  /* Fix footer alignment */
  footer {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    gap: 0.2rem;
  }

  /* Show total page number */
  section::after {
    content: attr(data-marpit-pagination) ' / ' attr(data-marpit-pagination-total);
    width: auto;
  }
---

<!--
paginate: true
footer: "Pitch #3 | 2024-10-28"
-->

<!--
_paginate: skip
-->

# <!--fit--> Lodipon

## Team & task distribution

| Name         | Component                             |
| :----------- | :------------------------------------ |
| Huanbo Meng  | Anomaly detection                     |
| Jinrui Zhang | Velocity analysis                     |
| Luka Leer    | Visualisation & presentation          |
| Wahab Ahmed  | Anomaly detection                     |

## Progress so far

### Visualisation

* Monday morning flu
* Monday afternoon flu
* Monday evening flu
* Tuesday flu
* Wednesday flu
* Thursday flu

### The challenge of using an accelerometer to measure velocity

* **Acceleration** is, by definition, only the **rate of change**.
* Relative to its **containing system**
* Simpler and accurate enough: just use **GPS data!**

### Anomaly detection

* **Naive version:** 10% deviation.
* **Improvements:** running patterns and rhythm matching.
* **Localisation:** trilateration using distance from beacons (RSSI)

## Coming up

![w:30cm](asset/planning.svg)

## Any questions?
