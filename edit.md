---
layout: default
---
# How to edit the pages in https://icyphy.github.io/ptII/

The static pages like this one are edited via the `gh-pages` branch of the [Github ptII repo](https://github.com/icyphy/ptII)

```
      git clone --depth=50 --branch=master --single-branch --branch=gh-pages https://github.com/icyphy/ptII gh-pages 
```

See [https://pages.github.com/](https://pages.github.com/) for details.
    
The pages in the [logs](logs/index.html) and [downloads](downloads/index.html) directory are created as part ofthe [Travis-ci ptII build](https://travis-ci/icyphy/ptII), which runs [$PTII/bin/ptIITravisBuild.sh](https://github.com/icyphy/ptII/blob/master/bin/ptIITravisBuild.sh)

---
* [Back up to the top](index.html)
