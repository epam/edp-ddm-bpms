{{/*
Expand the name of the chart.
*/}}
{{- define "bpms.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "bpms.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "bpms.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "imageRegistry" -}}
{{- if .Values.global.imageRegistry -}}
{{- printf "%s/" .Values.global.imageRegistry -}}
{{- else -}}
{{- end -}}
{{- end }}

{{- define "bpms.url" -}}
{{- printf "%s%s-%s-%s.%s" "https://" (include "bpms.name" .) .Values.cdPipelineName .Values.cdPipelineStageName .Values.dnsWildcard }}
{{- end }}

{{- define "keycloak.citizenTargetRealm" -}}
{{- printf "%s-%s" .Values.namespace .Values.keycloak.citizenClient.realm }}
{{- end -}}

{{- define "keycloak.officerTargetRealm" -}}
{{- printf "%s-%s" .Values.namespace .Values.keycloak.officerClient.realm }}
{{- end -}}

{{- define "keycloak.url" -}}
{{- printf "%s%s" "https://" .Values.keycloak.host }}
{{- end -}}

{{- define "keycloak.urlPrefix" -}}
{{- printf "%s%s%s" (include "keycloak.url" .) "/auth/realms/" .Release.Namespace -}}
{{- end -}}

{{- define "issuer.officer" -}}
{{- printf "%s-%s" (include "keycloak.urlPrefix" .) .Values.keycloak.officerClient.realm -}}
{{- end -}}

{{- define "issuer.citizen" -}}
{{- printf "%s-%s" (include "keycloak.urlPrefix" .) .Values.keycloak.citizenClient.realm -}}
{{- end -}}

{{- define "issuer.admin" -}}
{{- printf "%s-%s" (include "keycloak.urlPrefix" .) .Values.keycloak.realms.admin -}}
{{- end -}}

{{- define "issuer.external" -}}
{{- printf "%s-%s" (include "keycloak.urlPrefix" .) .Values.keycloak.realms.external -}}
{{- end -}}

{{- define "jwksUri.officer" -}}
{{- printf "%s-%s%s" (include "keycloak.urlPrefix" .) .Values.keycloak.officerClient.realm .Values.keycloak.certificatesEndpoint -}}
{{- end -}}

{{- define "jwksUri.citizen" -}}
{{- printf "%s-%s%s" (include "keycloak.urlPrefix" .) .Values.keycloak.citizenClient.realm .Values.keycloak.certificatesEndpoint -}}
{{- end -}}

{{- define "jwksUri.admin" -}}
{{- printf "%s-%s%s" (include "keycloak.urlPrefix" .) .Values.keycloak.realms.admin .Values.keycloak.certificatesEndpoint -}}
{{- end -}}

{{- define "jwksUri.external" -}}
{{- printf "%s-%s%s" (include "keycloak.urlPrefix" .) .Values.keycloak.realms.external .Values.keycloak.certificatesEndpoint -}}
{{- end -}}
