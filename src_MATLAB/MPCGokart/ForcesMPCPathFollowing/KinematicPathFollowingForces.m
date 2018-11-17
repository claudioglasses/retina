%add force path (change that for yourself)
addpath('/home/marc/Forces')
addpath('..');
addpath('casadi');
    
clear model
clear problem
clear all
close all

maxSpeed = 10;
pointsO = 1;
pointsN = 10;
splinestart = 1;
nextsplinepoints = 0;
%parameters: p = [maxspeed, pointsx, pointsy]

% variables z = [ab,dotbeta,ds,x,y,theta,v,beta,s,braketemp]
global index
index.ab = 1;
index.dotbeta = 2;
index.ds = 3;
index.x = 4;
index.y = 5;
index.theta = 6;
index.v = 7;
index.beta = 8;
index.s = 9;
index.braketemp = 10;
index.ns = 7;
index.nu = 3;
index.nv = index.ns+index.nu;
index.sb = index.nu+1;
index.ps = 1;

integrator_stepsize = 0.1;

model.N = 31;
model.nvar = 10;
model.neq = 7;

model.eq = @(z,p) RK4( z(index.sb:end), z([index.ab,index.dotbeta,index.ds]), @(x,u,p)interstagedx(x,u), integrator_stepsize,p);
model.E = [zeros(index.ns,index.nu), eye(index.ns)];

l = 1;

%limit lateral acceleration
model.nh = 3; 
model.ineq = @(z,p) nlconst(z,p,getPointsFromParameters(p, pointsO, pointsN));
model.hu = [36,0,0];
model.hl = [-inf,-inf, -inf];


%points = [1,2,2,4,2,2,1;0,0,5.7,6,6.3,10,10]';
points = [0,40,40,5,0;0,0,10,9,10]';
trajectorytimestep = integrator_stepsize;
[p,steps,speed,ttpos]=getTrajectory(points,2,1,trajectorytimestep);

model.npar = pointsO + 2*pointsN;
for i=1:model.N-1
   model.objective{i} = @(z,p)objective(z,getPointsFromParameters(p, pointsO, pointsN),p(index.ps));
end
model.objective{model.N} = @(z,p)objectiveN(z,getPointsFromParameters(p, pointsO, pointsN),p(index.ps));

model.xinitidx = index.sb:index.nv;
% variables z = [ab,dotbeta,ds,x,y,theta,v,beta,s,braketemp]
model.ub = ones(1,index.nv)*inf;
model.lb = -ones(1,index.nv)*inf;
model.ub(index.dotbeta)=5;
model.lb(index.dotbeta)=-5;
model.ub(index.ds)=1.6;
model.lb(index.ds)=-0.1;
model.lb(index.v)=0;
model.ub(index.beta)=0.45;
model.lb(index.beta)=-0.45;
model.ub(index.s)=pointsN-2;
model.lb(index.s)=0;
model.ub(index.braketemp)=85;

%model.ub = [inf, +5, 1.6, +inf, +inf, +inf, +inf,0.45,pointsN-2,85];  % simple upper bounds 
%model.lb = [-inf, -5, -0.1, -inf, -inf,  -inf, 0,-0.45,0,-inf];  % simple lower bounds 
codeoptions = getOptions('MPCPathFollowing');
codeoptions.maxit = 200;    % Maximum number of iterations
codeoptions.printlevel = 2; % Use printlevel = 2 to print progress (but not for timings)
codeoptions.optlevel = 2;   % 0: no optimization, 1: optimize for size, 2: optimize for speed, 3: optimize for size & speed
codeoptions.cleanup = false;
codeoptions.timing = 1;

output = newOutput('alldata', 1:model.N, 1:model.nvar);

FORCES_NLP(model, codeoptions,output);

tend = 100;
eulersteps = 10;
xs = [20,0,0,1,0,0.1,70];
%[...,x,y,theta,v,beta,s,braketemp]
xs(index.x-index.nu)=20;
xs(index.y-index.nu)=0;
xs(index.theta-index.nu)=0;
xs(index.v-index.nu)=1;
xs(index.beta-index.nu)=0;
xs(index.s-index.nu)=0.1;
xs(index.braketemp-index.nu)=60;
history = zeros(tend*eulersteps,model.nvar+1);
x0 = [zeros(model.N,index.nu),repmat(xs,model.N,1)]';
%x0 = zeros(model.N*model.nvar,1); 
tstart = 1;
paras = ttpos(tstart:tstart+model.N-1,2:3)';
for i =1:tend
    tstart = i;
    %model.xinit = [0,5,0,0.1,0,0];

    %find bspline
    if xs(6)>1
        nextSplinePoints
        %spline step forward
        splinestart = splinestart+1;
        xs(index.s-index.nu)=xs(index.s-index.nu)-1;
        %if(splinestart>pointsN)
            %splinestart = splinestart-pointsN;
        %end
    end
    %xs(6)=xs(6)+normrnd(0,0.04);
    problem.xinit = xs';
    %do it every time because we don't care about the performance of this
    %script
    ip = splinestart;
    [nkp, ~] = size(points);
    nextSplinePoints = zeros(pointsN,2);
    for i=1:pointsN
       while ip>nkp
            ip = ip -nkp;
       end
       nextSplinePoints(i,:)=points(ip,:);
       ip = ip + 1;
    end
    
    
    %paras = ttpos(tstart:tstart+model.N-1,2:3)';
    problem.all_parameters = repmat (getParameters(maxSpeed,nextSplinePoints) , model.N ,1);
    %problem.all_parameters = zeros(22,1);
    problem.x0 = x0(:);
    %problem.x0 = zeros(310,1);
    
    % solve mpc
    [output,exitflag,info] = MPCPathFollowing(problem);
    nextSplinePoints
    %get output
    outputM = reshape(output.alldata,[model.nvar,model.N])';
    x0 = outputM';
    u = repmat(outputM(1,1:index.nu),eulersteps,1);
    [xhist,time] = euler(@(x,u)interstagedx(x,u),xs,u,integrator_stepsize/eulersteps);
    xs = xhist(end,:);
    xs
    history((tstart-1)*eulersteps+1:(tstart)*eulersteps,:)=[time(1:end-1)+(tstart-1)*integrator_stepsize,u,xhist(1:end-1,:)];
end
%[t,ab,dotbeta,x,y,theta,v,beta,s]
draw

