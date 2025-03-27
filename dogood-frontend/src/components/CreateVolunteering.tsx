import { useForm, SubmitHandler } from 'react-hook-form'; 
import { useParams } from "react-router-dom";
import { createVolunteering } from '../api/organization_api';
import { useNavigate } from 'react-router-dom';
import './../css/CreateVolunteering.css'
import './../css/CommonElements.css'

interface VolunteeringFormData {
  name: string;
  description: string;
}

function CreateVolunteering() {
  const navigate = useNavigate();
  let { id } = useParams(); 

  const { register, handleSubmit, formState: { errors } , reset} = useForm<VolunteeringFormData>();
  
    const contactSubmit: SubmitHandler<VolunteeringFormData> = async (data) => {
        try {
            if(id !== undefined) {
              await createVolunteering(parseInt(id), data.name, data.description);
              alert("Volunteering created successfully!");
              navigate(-1);
            }
            else {
              alert("Error");
            }
        }
        catch(e){
          //send to error page
          alert(e);
        }
    };

    /*return (
      <form className = "create-volunteering-form" onSubmit={handleSubmit(contactSubmit)}>
        <h1>Create Volunteering</h1>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <label htmlFor="name">Volunteering Name:</label>
          <input
            id="name"
            {...register('name', { 
              required: 'Volunteering name is required',
              minLength: {
                value: 3,
                message: 'Must be at least 3 characters'
              },
              maxLength: {
                value: 50,
                message: 'Cannot exceed 50 characters'
              }
            })}
          />
          {errors.name && <p style={{ color: 'red' }}>{errors.name.message}</p>}
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <label htmlFor="description">Volunteering Description:</label>
          <input
            id="description"
            {...register('description', { 
              required: 'Volunteering description is required',
              minLength: {
                value: 2,
                message: 'Must be at least 2 characters'
              },
              maxLength: {
                value: 300,
                message: 'Cannot exceed 300 characters'
              }
            })}
          />
          {errors.description && <p style={{ color: 'red' }}>{errors.description.message}</p>}
        </div>

        <button type="submit">Create Volunteering</button>
      </form>
    );*/

    return (
      <form className="create-organization-form" onSubmit={handleSubmit(contactSubmit)}>
        <div className="form-container">
          <div className='createPostHeaders'>
            <h1 className="bigHeader">Create A New Volunteering</h1>
            <h2 className="smallHeader">Create a volunteering – inspire others, build community, and make a difference</h2>
          </div>

          <div className="form-fields">
            <div className="form-group">
              <label className="smallHeader" htmlFor="name">Name:</label>
              <input
            id="name"
            {...register('name', { 
              required: 'Volunteering name is required',
              minLength: {
                value: 3,
                message: 'Must be at least 3 characters'
              },
              maxLength: {
                value: 50,
                message: 'Cannot exceed 50 characters'
              }
            })}
          />
              {errors.name && <p>{errors.name.message}</p>}
            </div>

            <div className="form-group">
              <label className="smallHeader" htmlFor="description">Description:</label>
              <input
                id="description"
                {...register('description', { 
                  required: 'Volunteering description is required',
                  minLength: {
                    value: 2,
                    message: 'Must be at least 2 characters'
                  },
                  maxLength: {
                    value: 300,
                    message: 'Cannot exceed 300 characters'
                  }
                })}
              />
              {errors.description && <p>{errors.description.message}</p>}
            </div>

            <button className='orangeCircularButton' type="submit">
              create Volunteering
            </button>
          </div>
        </div>
      </form>

    )
}

export default CreateVolunteering