import React, { useEffect, useState } from 'react';
import { useForm, SubmitHandler } from 'react-hook-form'; 
import { useParams } from "react-router-dom";
import { createOrganization, editOrganization, getOrganization } from '../api/organization_api';
import { useNavigate } from 'react-router-dom';
import OrganizationModel from '../models/OrganizationModel';
import './../css/CreateOrganization.css'
import './../css/CommonElements.css'
import StepIndicator from './Stepper';

interface OrganizationFormData {
  name: string;
  description: string;
  phoneNumber: string;
  email: string;
}

function CreateOrganization() {
  const navigate = useNavigate();
  let { id, quickstart } = useParams(); 

  const { register, handleSubmit, formState: { errors } , reset} = useForm<OrganizationFormData>();
  const [edit, setEdit] = useState(false);
  const [organization, setOrganization] = useState<OrganizationModel | null>(null);

    const isEdit = async () => {
      if(id !== undefined && parseInt(id) !== -1) {
        setEdit(true);

        let organization: OrganizationModel = await getOrganization(parseInt(id));
        setOrganization(organization);
        reset({
          name: organization.name || '',
          description: organization.description || '',
          phoneNumber: organization.phoneNumber || '',
          email: organization.email || ''
        });
      }
      else {
        setEdit(false);
        setOrganization(null);
      }
    }
  
    const contactSubmit: SubmitHandler<OrganizationFormData> = async (data) => {

      let organizationId: number = -1;
        try {
          if(!edit) {
            organizationId = await createOrganization(data.name, data.description, data.email, data.phoneNumber);
            alert("Organization created successfully!");
            if(quickstart === "1") {
              navigate(`/organization/${organizationId}/createVolunteering/1`);
            }
            else {
              navigate(`/organization/${organizationId}`);
            }
          }
          else {
            if(id === undefined) {
              alert("Error");

            }
            else {
              organizationId = parseInt(id);
              await editOrganization(organizationId, data.name, data.description, data.email, data.phoneNumber);
              alert("Organization edited successfully!");
              navigate(`/organization/${organizationId}`);
            } 
          }
        }
        catch(e){
          //send to error page
          alert(e);
        }
    };

    useEffect(() => {
      isEdit();
    }, [id])

    return (
      <form className="create-organization-form" onSubmit={handleSubmit(contactSubmit)}>
        {quickstart === "1" && <StepIndicator activeStep={0} />}
        <div className="form-container">
          <div className='createHeaders'>
          
            <h1 className="bigHeader">{edit ? "Edit Organization" : "Create Organization"}</h1>
            <h2 className="smallHeader">{edit ? "Edit " : "Start "}your organization now and make your vision a reality - build a community that shares your passion</h2>
          </div>

          <div className="form-fields">
            <div className="form-group">
              <label className="smallHeader" htmlFor="name">Name:</label>
              <input
                id="name"
                defaultValue={edit ? organization?.name : ''}
                {...register('name', { 
                  required: 'Organization name is required',
                  minLength: { value: 3, message: 'Must be at least 3 characters' },
                  maxLength: { value: 50, message: 'Cannot exceed 50 characters' }
                })}
              />
              {errors.name && <p>{errors.name.message}</p>}
            </div>

            <div className="form-group">
              <label className="smallHeader" htmlFor="description">Description:</label>
              <input
                id="description"
                defaultValue={edit ? organization?.description : ''}
                {...register('description', { 
                  required: 'Organization description is required',
                  minLength: { value: 2, message: 'Must be at least 2 characters' },
                  maxLength: { value: 300, message: 'Cannot exceed 300 characters' }
                })}
              />
              {errors.description && <p>{errors.description.message}</p>}
            </div>

            <div className="form-group">
              <label className="smallHeader" htmlFor="email">Email:</label>
              <input 
                id="email"
                defaultValue={edit ? organization?.email : ''}
                {...register('email', { 
                  required: 'Organization email is required',
                  pattern: {
                    value: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
                    message: 'Invalid email address'
                  }
                })}
              />
              {errors.email && <p>{errors.email.message}</p>}
            </div>

            <div className="form-group">
              <label className="smallHeader" htmlFor="phoneNumber">Phone Number:</label>
              <input
                id="phoneNumber"
                defaultValue={edit ? organization?.phoneNumber : ''}
                {...register('phoneNumber', { 
                  required: 'Organization phone number is required',
                  pattern: {
                    value: /^(\+972|0)5\d-?\d{7}$/,
                    message: 'Invalid phone number'
                  }
                })}
              />
              {errors.phoneNumber && <p>{errors.phoneNumber.message}</p>}
            </div>

            <button className='orangeCircularButton' type="submit">
              {edit ? 'Save Changes' : (quickstart === "1" ? 'Continue To Creating Volunteering →' : 'Create Organization')}
            </button>
          </div>
        </div>
      </form>

    );
}

export default CreateOrganization