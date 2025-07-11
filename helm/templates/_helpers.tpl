{{- define "file-management-service.name" -}}
{{ .Chart.Name }}
{{- end }}

{{- define "file-management-service.fullname" -}}
{{ printf "%s-%s" .Release.Name .Chart.Name }}
{{- end }}
