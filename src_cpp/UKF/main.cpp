//
// Created by maximilien on 22.05.19.
//
#include <iostream>
#include "src/TestUKF.h"
#include "src/TestPacejkaUKF.h"

using namespace std;
int main()
{
    cout << "test pacejka UKF............................... " << endl;
    TestPacejkaUKF testPacejkaUkf;
    testPacejkaUkf.test();

    //cout << "test UKF................................" << endl;
    //TestUKF testUkf;
    //testUkf.test();

}