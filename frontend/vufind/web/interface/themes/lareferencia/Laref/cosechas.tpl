
<h1>Pa&iacute;ses Socios</h1>

<h2>Material por Fecha de Recolecci&oacute;n</h2> 
{literal}
      <style type="text/css">
 
	        #container3 {
        width : 600px;
        height: 384px;
        margin: 8px auto;
      }
    </style>
{/literal}
<div id="container3"> </div>
 <script type="text/javascript" src="{$url}/flotr2.min.js"></script>
{literal}
<script type="text/javascript">
(
function basic_time(container) {
{/literal} 
{$output8}
{literal}ax;
    options = {
	
        xaxis: {
            title: 'Fecha',
            labelsAngle: 0,
		noTicks: 7, 
		mode:"time",
		timeformat: "%y",
		minTickSize: [1, "week"]
        },
        yaxis: {
            title: 'Registros',
			max:25000 
        },	
        mouse: {
            track: true,
            relative: true
        },
        HtmlText: false,
        title: 'Registros por Fecha'
    };

    // Draw graph with default options, overwriting with passed options


    function drawGraph(opts) {

        // Clone the options, so the 'options' variable always keeps intact.
        o = Flotr._.extend(Flotr._.clone(options), opts || {});

        // Return a new graph.
        return Flotr.draw(
        container,
[{
        data: d1,
        label: 'AR'
    }, {
        data: d2,
        label: 'BR'
    }, {
        data: d3,
        label: 'CL',
    }, {
        data: d4,
        label: 'CO'
    },{
        data: d5,
        label: 'EC'
    },{
        data: d6,
        label: 'SV'
    },{
        data: d7,
        label: 'MX'
    },{
        data: d8,
        label: 'VE'
    },	
	{
        data: d9,
        label: 'PE'
    }],	
		 o);
    }

    graph = drawGraph();

  })
( container=document.getElementById("container3")
);
    </script>
{/literal} 	
<div class="clear"></div>
<h2>Estad&iacute;sticas de Cosechas</h2>
{$output7}

<div class="clear"></div>