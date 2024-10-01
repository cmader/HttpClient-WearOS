# HttpClient-WearOS

HttpClient is an app that is currently available for Garmin (on the 
[connect iq store](https://apps.garmin.com/apps/da241207-e929-4cdf-9662-11ab17ffd70d)) and Wear OS 
based smartwatches (as APK [here](https://github.com/cmader/HttpClient-WearOS)). It allows you to 
send HTTP GET requests from your watch to configurable URLs. You can give these requests a custom 
name and organize them in a hierarchy which is browsable on the watch in a convenient way.

## Description

As an input, the app requires an **Endpoints Definition** which is a textual description of all the 
requests that should be selectable on the watch. Here's an example:

```
- all,All
-- off,Office
--- shut_opn,Open shutter,/shut/office/open
--- shut_cls,Close shutter,/shut/office/close
-- liv,Living Room
--- l_on,Lights on,https://192.168.8.1/light/liv/on
-- kit,Kitchen
```

As you can see in the example above, the endpoint definition is structured hierarchically with the 
hierarchy level indicated by `-` characters. This level indicator is then followed by a unique 
identifier. After that, the request's human readably name as it will be shown on the watch display 
is given, and finally the HTTP request URL. All these elements are separated by comma.

The request url can be given both in "relative" format (i.e., providing only a path as shown 
with the office requests) or in absolute format, including protocol and host as it is the case with 
the living room request. If you use "relative" URLs, the app automatically adds a "Base URL" in 
front of the paths (see Section "App Configuration").

## App Configuration

HttpClient needs to access the Endpoints Definition from the (local) network. This is done by
providing an **Endpoints base URL** in the app's configuration screen (see Section "App
Configuration"). The app internally appends the path `/endpoints` to the Endpoints base URL. So if
you set your Endpoints base URL to, for instance, `https://my.host.net`, HttpClient tries to fetch
the Endpoints Definition from `https://my.host.net/endpoints`.



## Installation
### Compatibility

## Open Issues and Next Steps

* Improve error handling on endpoint definition parsing
* Optimize performance

## Distribution and License
[HttpClient for Wear OS](https://github.com/cmader/HttpClient-WearOS) © 2024 by 
[Christian Mader](https://github.com/cmader) is licensed under 
[CC BY-NC-ND 4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/) 

## Contributors
* [Christian Mader](https://github.com/cmader), Developer
