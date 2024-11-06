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
_footer: "Demo | 2024-11-06"
-->

<!--
_paginate: skip
-->

# <!--fit--> Lodipon

## Team & task distribution

| Name         | Component                                 |
| :----------- | :---------------------------------------- |
| Huanbo Meng  | Anomaly detection                         |
| Jinrui Zhang | Velocity analysis                         |
| Luka Leer    | Visualisation & integration |
| Wahab Ahmed  | Localisation & anomaly detection           |

## What is Lodipon?

A **running app** that:

* helps you **stay consistent** when running **longer distances**;
* **detects deviations** and gives **real-time feedback**;
* and uses **beacons as checkpoints** to **precisely track** your speed and segment time.

## How does it work?

### Sensor gathering

* **Fused location provider**
  * Built-in sensor accumulation
  * Best-effort measure
* To account for **sensor noise**, we used a **sliding window**.

![bg right contain](asset/sensor-gathering.svg)

### Anomaly detection

* **Threshold-based**
* Two modes
* **Initial** values are -2.5% and +3.5%
  * Can be adjusted

![bg left height:90%](asset/anomaly-detection.svg)

### Localisation

* **BLE beacons** as **checkpoints**
* **RSSI** for **distance estimation**
* **Sensitivity** and **distance** can be adjusted

![bg right height:90%](asset/localisation.svg)

### Visualisation & integration

* **Simple** and **intuitive**
* Interaction through **shared view model**
* **Real-time** updates

![bg left height:90%](asset/interaction.svg)

## Demo time!

![](asset/demo.png)

---

<!--
_paginate: skip
-->

<video controls muted height="640">
  <source src="asset/demo.mp4" type="video/mp4">
</video>

## Any questions?
