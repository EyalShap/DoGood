import React from 'react';
import { Stepper, Step, StepLabel } from '@mui/material';

interface StepIndicatorProps {
  activeStep: number; // 0, 1, 2
}

const steps = ['Create Organization', 'Add Volunteering', 'Post'];

const StepIndicator: React.FC<StepIndicatorProps> = ({ activeStep }) => {
  return (
    <div style={{ margin: '20px 0' }}>
      <Stepper activeStep={activeStep} alternativeLabel>
        {steps.map((label) => (
          <Step key={label}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>
    </div>
  );
};

export default StepIndicator;
