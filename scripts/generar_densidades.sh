#!/usr/bin/env bash
# Genera variantes por densidad de un PNG de composeResources/drawable.
#
# Uso:   scripts/generar_densidades.sh <nombre.png> <ancho_en_dp>
# Ej.:   scripts/generar_densidades.sh panel_marco.png 250
#
# <ancho_en_dp> = ancho con el que se verá la imagen entera en pantalla
# (para un 9-slice, el mismo valor que anchoDiseno en DndSkins).
#
# Crea drawable-mdpi (1x), -hdpi (1.5x), -xhdpi (2x), -xxhdpi (3x) y -xxxhdpi (4x)
# con el mismo nombre. Nunca amplía: si el original es más pequeño que el tamaño
# pedido, la variante se queda con el tamaño del original.
# El PNG de drawable/ no se toca; sigue sirviendo cuando no encaja ninguna variante.
# Necesita ImageMagick (magick).
set -euo pipefail

if [ $# -ne 2 ]; then
    echo "Uso: $0 <nombre.png> <ancho_en_dp>" >&2
    exit 1
fi

nombre=$1
ancho_dp=$2
base="$(cd "$(dirname "$0")/.." && pwd)/shared/src/commonMain/composeResources"
origen="$base/drawable/$nombre"

if [ ! -f "$origen" ]; then
    echo "No existe $origen" >&2
    exit 1
fi

for par in mdpi:1 hdpi:1.5 xhdpi:2 xxhdpi:3 xxxhdpi:4; do
    carpeta=${par%%:*}
    factor=${par##*:}
    ancho_px=$(awk -v d="$ancho_dp" -v f="$factor" 'BEGIN { printf "%d", d * f + 0.5 }')
    mkdir -p "$base/drawable-$carpeta"
    # '>' = solo reduce, nunca amplía; -strip quita metadatos para ahorrar espacio
    magick "$origen" -filter Lanczos -resize "${ancho_px}x>" -strip "$base/drawable-$carpeta/$nombre"
    echo "drawable-$carpeta/$nombre -> $(magick identify -format '%wx%h' "$base/drawable-$carpeta/$nombre")"
done
