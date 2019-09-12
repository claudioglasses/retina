function [ACCX,ACCY,ACCROTZ] = nomodel_1x64_pureinputs(VELX,VELY,VELROTZ,BETA,AB,TV, param)    
    w1 = [0.7733002 -0.009973474 -0.24896646 -0.04123733 -0.59392226 0.855489 -0.24035494 -0.34505382 0.013162531 -0.69057816 -0.28179395 0.1555335 -0.027337197 -0.03230636 0.5999142 0.5821999 -0.36727473 -0.035552528 0.16779582 -0.07380475 0.4412007 0.004596799 -0.05016733 0.3152737 0.24508846 0.11472868 -0.1425718 -0.2227223 -0.36474913 -0.9581595 -0.31222278 -0.6081835 1.01868 -0.23371673 0.9811823 -0.49377143 -0.17592348 -0.04519266 0.017320463 0.1973756 0.686317 -0.53986096 -0.05741798 0.114078276 -0.13395485 -0.95893 -0.03435312 -0.28040352 0.03859406 -0.5047682 0.7342958 -0.34936494 0.4847289 0.92431796 0.08605642 0.20379654 0.54135096 -0.16282132 0.19519131 -0.5298981 0.7418281 -0.728431 0.3744021 0.76233244;-0.052217104 -0.81451464 0.082120486 -0.29440314 0.2445194 0.105297096 0.008495623 -0.06045359 -0.12308382 -0.10258456 0.17338723 0.40370306 0.056834094 0.028777806 -0.043199807 0.34729242 0.22039804 -0.20975351 -0.05948514 -0.0049622124 0.28337324 0.097353004 -0.18633032 -0.27678517 -0.04918751 -0.09449375 -0.058294423 -1.2480893 0.23982766 0.11253733 -0.1376567 -0.29511622 0.46089458 -0.027818233 0.081896186 0.4291632 0.10057865 0.24233146 -0.111147165 0.59296286 -0.10030648 0.034427363 -0.20213245 -0.2705934 -0.18360749 0.019073075 0.5902984 -0.074051425 -0.0383693 -0.23082644 -0.00025652832 -0.06662335 0.08199284 0.28243914 -0.11907255 0.4832602 -0.20251979 0.28546968 0.05727134 0.07342499 -0.70829725 0.108242795 -0.4837085 -0.2375623;-0.33092204 0.2435434 -0.22507192 -0.17712763 0.11814864 0.05035496 -0.27144963 0.40740567 -0.20909782 -0.022306338 0.25224558 -0.21553318 -0.40301347 0.23012239 -0.87741905 0.35843375 0.59214425 -0.5271423 -0.20972826 0.16139384 -1.0486685 -0.58847475 0.3497925 0.7488274 0.36074483 -0.3512323 -0.032345504 0.5736268 0.08159451 0.106946 0.51384574 -0.4634502 0.13703795 -0.045157384 0.3605339 0.51595974 -0.27012536 0.10966604 0.2865948 -0.8685715 0.44387096 -0.35323033 -0.38757464 -0.17921737 -0.71184117 0.06374763 -1.3271338 0.23614745 -0.27727476 0.061207477 -0.014946197 -0.091786146 0.3510023 -0.019631786 0.18044509 0.21153252 -0.05577334 0.75657034 0.26876283 0.2851526 -0.24831134 0.37928584 0.905741 0.27586427;0.10296871 -0.22908174 -0.04063544 0.34169567 -0.5767228 -0.15593621 0.092342906 0.02401575 0.58878213 -0.56256616 0.25626114 -0.15263504 1.0171001 -0.41459525 0.24708042 -1.4274683 -0.27786863 0.4775592 0.073728256 0.029999174 0.64575344 -0.33383128 0.34227145 -0.26012567 0.21954946 0.38078946 0.6266713 0.09833276 0.084852345 -0.5104414 -0.53506076 -0.1603624 -0.23769332 -0.016801702 -0.20591213 0.008090604 -0.14832152 -0.58281577 0.052515373 0.06308985 -0.08587943 0.034943108 -0.113040455 -0.038467504 -0.29508513 -0.39511514 1.1147615 0.0045774514 0.21132724 0.38100848 -0.542939 -0.047816247 0.26881647 0.05030221 0.099583186 -1.5151544 1.0787042 -1.2103925 -0.16163726 0.48111477 0.47591305 -0.31087068 -0.22806428 -0.553929;-0.01620008 -0.114455014 0.04800918 0.019806743 0.052346144 0.04680091 -0.010894591 -0.018851105 -0.06347485 -0.03965627 -0.3558304 0.76535 0.28224066 -0.025770381 -0.16043186 -0.026907515 0.12970395 -0.1064174 0.2215946 0.0014597136 0.12651953 -0.29132533 -0.28552806 0.40495417 -0.040286634 0.04541104 0.22712623 0.18791579 0.032304574 -0.048057422 -0.04734592 0.22465897 -0.029799802 -0.06900426 -0.069806874 0.15291387 0.08187732 -0.1612622 -0.36314517 -0.29476735 -0.32547998 -0.38827482 -0.2863019 0.26814502 -0.3815468 0.08216488 0.022717439 0.08192682 0.011957556 0.052501455 0.06673548 -0.1731769 0.059073694 0.17313899 0.3033684 -0.10545707 0.091746174 0.22825934 0.5031424 -0.0379213 -0.020419735 0.14623642 -0.0569684 -0.010763313;-0.14566582 -0.37651783 -0.11810198 -0.1225222 -0.17039844 -0.18943143 -0.016063994 0.049936205 -0.41456816 -0.102135815 0.16644412 -0.10917127 0.33236182 0.3071274 0.118934885 0.035744403 0.025983214 -0.077143036 -0.19766435 -0.010803524 0.006993421 -0.16205837 0.3355171 -0.07044411 -0.06757454 -0.18026122 -0.03449607 -0.12162117 0.12458637 -0.16883594 -0.1479171 0.030368572 0.21112521 0.0002948335 0.020162316 -0.059575215 -0.038690094 0.061496563 -0.003154217 0.032286745 -0.061623085 -0.032303527 0.20837845 0.0940781 0.074920565 -0.14876954 0.0031353035 -0.0006827156 -0.100725845 0.064798295 -0.015817398 -0.17928077 -0.05196307 0.04705063 0.20226794 0.06871148 -0.15349925 -0.14328331 0.094971955 -0.0744267 -0.20348932 0.09129032 0.03221035 -0.081918985];
    b1 = [0.5910305 -0.2425622 1.4541031 -0.13802439 -0.47557864 -0.48404527 0.9022567 2.0942118 0.06625761 -1.020275 -0.15350914 0.6425384 -0.63991064 0.027097072 -0.57731926 -1.0783265 0.18341778 -0.86305547 -0.75615907 0.09383536 -0.95812535 -0.19556707 -0.20591085 -1.1730422 0.5865827 -0.5417487 0.54654163 0.02064159 0.92228466 0.7832163 -0.61070764 0.14136784 -0.45448515 -0.56218904 0.068863444 -0.016409189 1.6671356 -0.63175327 -0.9878907 -1.0440534 -0.8027782 -0.61547786 -0.34620243 0.12211314 0.106941946 -0.7084348 0.037174385 1.5320301 -0.085434854 -0.7614507 0.96731603 -1.0917057 -1.6324936 0.5990845 1.1497722 -0.09065664 -1.1791676 0.36842936 -0.7218178 1.6287558 -0.36047164 0.3218282 -1.3541955 0.6034035];
    w2 = [-0.47941267 -0.72722304 0.50996697;0.0036598514 0.20213486 -0.67717475;0.24598953 1.313293 -0.49017555;-0.21422173 -0.25171712 -0.859514;-0.64012086 -0.5912152 0.45453602;0.16634072 -0.9867019 0.12371156;0.15472396 0.93879616 -0.34952086;0.714692 -1.7484828 0.36014733;-0.2273432 0.1947341 -0.7124942;0.7201476 0.38878533 1.0216558;-0.18755949 -0.41218653 0.23207441;-0.30731058 0.035914477 -0.01086426;0.14906433 0.11669541 0.9155508;-0.095183976 -0.07389651 0.52281314;0.030308776 -0.3291127 0.8149369;0.21519874 -0.2718539 -1.6455092;0.36229515 -0.23818372 -0.24236383;0.64644635 -0.047427043 -0.9315735;0.9256439 0.3994903 -0.21993543;-0.025116105 -0.36126 0.059556752;-0.12594138 -0.899018 1.0731107;0.041373234 -0.43247125 0.7316875;-0.06730192 0.50788206 -0.62067044;-0.0302743 1.2563959 -0.5082535;0.86290586 -0.1949159 -0.06838128;0.42496127 0.38763717 -0.64495057;0.3767998 -0.35372585 0.21988367;-0.13229585 -0.611812 0.38602373;0.15564342 0.59404963 0.77171993;-0.263014 -1.0996767 -0.17259285;-0.5930581 -0.39711452 0.5200395;0.1730957 0.5414789 0.5478179;-0.07189065 -0.66091365 -0.65772647;0.56992376 -0.07136317 0.015425479;-0.30618963 0.60306275 -0.61884385;-0.13703752 -0.16508743 -0.591936;0.50539464 1.4499172 -0.26683775;-0.4898353 0.27010882 0.62935257;-0.8365429 -0.3134971 0.33613235;-0.1688519 -0.98256344 0.79874426;-0.24810714 0.5074166 -0.76067066;-0.545086 -0.17665944 -0.48071575;0.39573127 0.054106537 -0.49129343;-0.21842752 -0.27549624 -0.7530554;-0.16931649 0.26946267 0.44288933;0.6988425 0.19005626 0.77992785;0.045562565 0.52316874 0.76305366;0.6062793 -1.2723806 -0.080092;0.0638172 0.33997345 -0.3342846;-0.86145896 -0.078864776 -0.51008564;-0.6322194 0.33277115 1.0526865;1.112362 -0.25300395 0.538417;-0.018986957 -1.139633 1.1084008;0.9428537 0.17519182 -0.22755526;0.41887012 -0.85949033 0.3851719;0.10948014 0.18519828 -1.1285418;-0.10968047 0.21027242 1.3553401;0.34938765 0.28419852 -0.8966963;0.7447993 -0.5906907 -0.03819636;-0.0278015 -0.5338985 1.3229396;0.09253588 0.3836668 0.7454736;0.50827354 0.21703145 -0.57075983;0.26877698 1.3240986 -0.5803775;0.3215854 -0.08399968 0.9639024];
    b2 = [-1.3497921 0.080688976 -0.07379458];
    means = [2.663266683539544 0.0009528629472263598 -0.03544623778958484 -0.023567331536838564 0.061441704473481136 -0.064820385714929];
    stds = [1.9018031823453208 0.3002068463085762 0.5459086934303092 0.20123624768155168 0.8255699013317864 0.63671794510735];
    
    input = [VELX,VELY,VELROTZ,BETA,AB,TV];

    normed_input = (input - means) ./ stds;

    h1 = tanh(normed_input * w1 + b1);
    disturbance = h1 * w2 + b2;
    
    ACCX = disturbance(1);
    ACCY = disturbance(2);
    ACCROTZ = disturbance(3);
end

