# Update Checker

If you want you can be informed of plugin or modules updates. Each release version was pushed on github since 1.14.0.
The update checker will call the github APIs and announce an update to OPs on login. You can configure it to 
automatically download updates.

```yaml
    update:
      plugin: announce
      modules: announce
    # valid values:
    # download: download updates and announce when update is installed
    # announce: only announce, do not download
    # everything else will disable the update check
```
