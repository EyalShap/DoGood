import React, { useState } from 'react';
import Select, { MultiValue } from 'react-select'
import './../css/MultipleSelectDropdown.css'

interface Option {
    value: string;
    label: string;
}

interface MultipleSelectDropdownProps {
  label: string; 
  options: string[];
  onChange: (selectedValues: string[]) => void;
}

const MultipleSelectDropdown: React.FC<MultipleSelectDropdownProps> = ({ label, options, onChange }) => {
    const formattedOptions = options.map((option) => ({
        value: option,
        label: option,
      }));

    const [selectedValues, setSelectedValues] = useState<MultiValue<Option>>([]);

  const handleChange = (selected: MultiValue<Option>) => {
    setSelectedValues(selected);
    const selectedValues = selected.map(option => option.value);
    onChange(selectedValues);
  };

  return (
    <div className='dropdown'>
        <label className='dropdownTitle'>{label}</label>
        <Select className='actualDropdown'
            isMulti
            options={formattedOptions}
            value={selectedValues}
            onChange={handleChange}
        />
    </div>
  );
};

export default MultipleSelectDropdown;
