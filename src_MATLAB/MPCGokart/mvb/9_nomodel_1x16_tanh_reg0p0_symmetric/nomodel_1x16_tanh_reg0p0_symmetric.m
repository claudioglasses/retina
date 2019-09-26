function [ACCX,ACCY,ACCROTZ] = nomodel_1x16_tanh_reg0p0_symmetric(VELX,VELY,VELROTZ,BETA,AB,TV, param)    
    w1 = [0.8682441 -0.38758034 0.3695394 0.41060394 -0.96607983 1.301102 -0.4817559 -0.49683702 0.6328984 0.91558397 -0.8897263 -0.21265613 -0.36448327 -0.7326272 0.4257549 -0.11730206;-0.17846602 -0.026563704 0.09074664 0.031652045 -0.1847462 0.111357376 0.032024834 -1.13173 -0.12186817 -1.570888 0.33711684 -0.5370998 0.056800984 -0.44596142 -0.09128765 0.49587855;-0.20956391 -0.10386983 -0.40183294 -0.58080345 -0.29422793 -0.5175015 0.6267164 -2.2583935 0.16587602 -2.060381 -1.3703626 -0.0013078719 0.19792221 -0.22714727 -0.2211593 -1.2210948;-0.67941463 -0.22595963 -0.18715651 1.9337236 0.38477618 0.017132131 -0.045171373 3.31413 0.13176121 1.963669 1.2300042 0.82234836 -0.046809915 1.5613017 -0.04087814 0.22684275;0.039917465 -0.12592404 0.08581504 -0.059623275 -0.03838636 0.09292118 0.22411665 0.23700878 0.142908 -0.32555747 -0.19250588 -0.21526174 0.1241164 0.026156517 0.115860455 0.034829244;0.0009067886 -0.08563083 -0.040908203 -0.04298222 0.094971605 0.19006531 0.21044476 0.10023056 -0.08692092 0.19104864 0.16805549 -0.24878989 0.1124986 -0.13100226 0.08593729 0.19686812];
    b1 = [-2.5677147 -1.1663166 -2.8520486 -2.6543655 0.9752779 -3.5508454 2.9935703 0.59942913 0.6353076 -3.346889 2.3170455 1.9888391 0.117947884 1.6991383 -1.4976499 1.2296637];
    w2 = [-1.1133833;5.6410785;-5.4384346;-0.2933204;-1.0357463;0.31932953;0.38387674;0.09299684;3.877319;-0.01850311;0.3977142;-0.018155856;4.8844347;0.46382236;5.7167583;0.15971822];
    w3 = [-0.2364485 -3.2297745;-1.2939372 5.8102756;5.8328185 -1.2466346;-0.21718754 -1.8297006;2.7232835 2.122152;1.9217432 1.4150678;-3.2080977 3.6123796;-0.12653337 0.66455615;-1.1101251 -0.5871504;-0.6144627 -0.8891625;-1.0381479 1.3488636;2.4977784 -1.5471269;-0.3658559 0.07203774;-0.23976432 3.918756;2.412572 0.18002403;-1.531733 3.1881166];
    means = [2.6775749832578986 0.001922168301437502 -0.04515304867653743 -0.026195759869689427 0.06460829512732788 -0.0686602322088558];
    stds = [1.907241556285694 0.3079627568782051 0.5515613235298231 0.20101736434516326 0.8309067855283836 0.634574245360539];

    input = [VELX - means(1),VELY,VELROTZ,BETA,AB - means(5),TV];

    normed_input = input ./ stds;
    normed_input_neg = normed_input .* [1,-1,-1,-1,1,-1];

    h1 = tanh(normed_input * w1 + b1);
    h1_neg = tanh(normed_input_neg * w1 + b1);
    
    h1_even = (h1 + h1_neg) / 2;
    h1_odd = (h1 - h1_neg) / 2;
    
    ACC_odd = h1_odd * w3;
    
    ACCX = h1_even * w2;
    ACCY = ACC_odd(1);
    ACCROTZ = ACC_odd(2);
end

