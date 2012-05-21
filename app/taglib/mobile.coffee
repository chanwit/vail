fs   = require 'fs'
util = require 'util'

class MobileTagLib

    @namespace: 'mobile'

    @resources: () ->
        jquery = '1.7.2'
        jqueryMobile = '1.1.0'
        """
            <link rel=\"stylesheet\" href=\"css/jquery.mobile-#{jqueryMobile}.css\"/>
            <script src=\"js/jquery-#{jquery}.js\"></script>
            <script src=\"js/jquery.mobile-#{jqueryMobile}.js\"></script>
        """

    page: (attr, body) ->
        attr['data-role']  = 'page'
        attr['data-apply'] = attr.remove('apply')
        a = attr.toString()
        @out "<div #{a}>"
        @out body()
        @out '</div>'

    content: (attr, body) ->
        attr['data-role']  = 'content'
        a = attr.toString()
        @out "<div #{a}>"
        @out body()
        @out '</div>'

    button: (attr, body) ->
        attr['data-icon']   = attr.remove('icon')   if attr['icon']
        attr['data-mini']   = attr.remove('mini')   if attr['mini']
        attr['data-inline'] = attr.remove('inline') if attr['inline']
        attr['data-role'] = "button"
        text = body()
        tag = "button"
        if text == ''
            attr['data-iconpos'] = 'notext'
            text = attr['data-icon']
            tag = "a"
        a = attr.toString()
        @out "<#{tag} #{a}>#{text}</#{tag}>"

    header: (attr, body) ->
        attr['data-role']  = 'header'
        attr['data-theme'] = 'b'
        attr.prefix('data-','position')
        caption = attr.remove('caption')
        a = attr.toString()
        @out "<div #{a}>"
        @out body()
        @out "<h1>#{caption}</h1></div>"

    footer: (attr, body) ->
        attr['data-role']  = 'footer'
        caption = attr.remove('caption')
        attr.prefix('data-','position')

        a = attr.toString()
        @out "<div #{a}><h3>"
        @out caption
        @out '</h3></div>'

    listview: (attr, body) ->
        attr['data-role']  = 'listview'
        attr['data-inset'] = 'true'      # if !(attr['data-inset'])
        attr['data-theme'] = 'b'         # if !(attr['data-theme'])
        attr['data-dividertheme'] = 'c'  # if !attr['data-dividertheme']
        a = attr.toString()
        @out "<ul #{a}>"
        @out body()
        @out "</ul>"

    'list-divider': (attr, body) ->
        attr['data-role']  = 'list-divider'
        a = attr.toString()
        @out "<li #{a}>#{body()}</li>"

    listitem: (attr, body) ->
        a = attr.toString()
        @out "<li><a #{a}>#{body()}</a></li>"

    dialog: (attr, body) ->
        attr['data-role']  = 'button'
        attr['data-rel' ]  = 'dialog'
        attr.prefix('data-','inline','true')
        attr.prefix('data-','transition','pop')
        a = attr.toString()
        @out "<a #{a}>#{body()}</a>"

    layout: (attr, body) ->
        @out "<div>"
        @out body()
        @out "</div>"

exports.MobileTagLib = MobileTagLib