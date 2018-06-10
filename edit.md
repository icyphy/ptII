---
layout: default
---
# Editing these pages

These pages appear at https://icyphy.github.io/ptII/.
Static pages like this one are edited via the `gh-pages` branch of the [Github ptII repo](https://github.com/icyphy/ptII):

```
      git clone --depth=1 --single-branch --branch=gh-pages https://github.com/icyphy/ptII gh-pages 
```

Page sources are in markdown.
To update them, simply push a revision.
It may take a few minutes for the update to appear.
See [https://pages.github.com/](https://pages.github.com/) for details.
    
The pages in the [logs](logs/index.html) and [downloads](downloads/index.html) directory are created as part ofthe [Travis-ci ptII build](https://travis-ci/icyphy/ptII), which runs [$PTII/bin/ptIITravisBuild.sh](https://github.com/icyphy/ptII/blob/master/bin/ptIITravisBuild.sh).

---
* [Back up to the top](index.html)
