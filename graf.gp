set title 'Propagação da noticia Falsa'
set term pngcairo
set output 'grafico.png'
set decimalsign ','
set encoding utf8
set datafile separator ';'
set ytics 100
set xtics 10
set ylabel 'População'
set xlabel 'Nº Dias'
set yrange [0:14]
set xrange [0:13]
plot 'graf.csv' using 1:2 with line title 'S', 'graf.csv' using 1:3 with line title 'I', 'graf.csv' using 1:4 with line title 'R'
