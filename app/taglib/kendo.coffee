fs   = require 'fs'
util = require 'util'

class KendoTagLib

    @namespace: 'kendo'

    @resources: () ->
        """
            <link rel=\"stylesheet\" href=\"css/kendo.common.css\"/>
            <link rel=\"stylesheet\" href=\"css/kendo.default.css\"/>
            <script src=\"js/kendo.web.js\"></script>
        """

    grid: (attr, body) ->
        id = '#' + attr['id']
        a = attr.toString()
        @out "<table #{a}>"
        @out body()
        @out "</table>"
        @out "<script> $(document).ready(function(){ $('#{id}').kendoGrid(); }); </script>"

    columns: (attr, body) ->
        @out "<thead><tr>"
        @out body()
        @out "</tr></thead>"

    # TODO data binding to each column
    column: (attr, body) ->
        caption = attr.remove('caption')
        @out "<th #{attr.toString()}>"
        @out caption
        @out "</th>"

    rows: (attr, body) ->
        @out "<tbody>"
        @out body()
        @out "</tbody>"

    row: (attr, body) ->
        @out "<tr>"
        @out body()
        @out "</tr>"

exports.KendoTagLib = KendoTagLib