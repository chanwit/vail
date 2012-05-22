class TodoView extends Backbone.View

	events:
		'click #color-input': 'updateConfig'

	initialize: ->
		@model.view = @

	updateConfig: (e) =>
		@model.set 'color': $('#color-input').val()


