
fs  = require 'fs'
sys = require 'sys'

class BaseTagLib

    xul: (attr, body) ->
        ns = attr.remove('xmlns')
        @config['defaultNS'] = ns
        for k, v of attr
            if k.indexOf('xmlns') == 0
                [_, tag] = k.split(':')
                @config[tag] = v

        outfile = (name for name in (fs.readdirSync 'target/js') when name.indexOf('out.') is 0)[0]
        @out """
        <html>
            <head>
                <title>#{@pageScope['title']}</title>
                <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>

        """
        @out(@headers[ns].apply() + '\n')
        for k, v of @config
            if (k != 'defaultNS') && !(typeof v is 'function')
                @out(@headers[v].apply() + '\n')

        @out """
                <script src=\"js/#{outfile}\"></script>
            </head>
            <body>
                #{body()}
            </body>
        </html>
        """

exports.BaseTagLib = BaseTagLib