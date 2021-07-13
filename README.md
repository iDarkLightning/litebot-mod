# LiteBot-Mod

LiteBot-Mod is a fabric mod that facilitates communication with [LiteBot](https://github.com/iDarkLightning/LiteBot) via a websocket connection.

## Installation

To install the mod, head over to the [releases page](https://github.com/iDarkLightning/litebot-mod/releases) and download the `.jar` file and insert it into your server's `mods` directory.

## Configuration

After the first run, a config file called `litebot.json` will be generated in your server's config directory.

Open up the JSON file and fill out the following fields:
* `server_name`: The name of the server as configured in LiteBot
* `litebotAddress`:The address that LiteBot is running on. Do not include the scheme (`localhost:8000` not `http://localhost:8000`)
* `secretKey`: The `api_secret` used in LiteBot's config that will be used to authenticate with LiteBot