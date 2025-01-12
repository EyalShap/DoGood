import { useForm, SubmitHandler } from 'react-hook-form'; 
import { useParams } from "react-router-dom";
import { useNavigate } from 'react-router-dom';
import { createVolunteeringPost, editVolunteeringPost, getVolunteeringPost } from '../api/post_api';
import React, { useEffect, useState } from 'react';
import { VolunteeringPostModel } from '../models/VolunteeringPostModel';
import './../css/CreateVolunteeringPost.css'

interface VolunteeringPostFormData {
  title: string;
  description: string;
}

function CreatePost() {
  const navigate = useNavigate();
  let { id, postId } = useParams();
  const [edit, setEdit] = useState(false);
  const [post, setPost] = useState<VolunteeringPostModel | null>(null);

  const { register, handleSubmit, formState: { errors } , reset} = useForm<VolunteeringPostFormData>();
  
  const isEdit = async () => {
    if(postId !== undefined && parseInt(postId) !== -1) {
      setEdit(true);

      let post: VolunteeringPostModel = await getVolunteeringPost(parseInt(postId));
      setPost(post);
      reset({
        title: post.title || '',
        description: post.description || ''
      });
    }
    else {
      setEdit(false);
      setPost(null);
    }
  }

  const contactSubmit: SubmitHandler<VolunteeringPostFormData> = async (data) => {
    try {
      if(id !== undefined) {
        if(!edit) { 
          let postId: number = await createVolunteeringPost(data.title, data.description, parseInt(id));
          alert("Volunteering post created successfully!");
          navigate(`/volunteeringPostList`);
        }
        else {
          if(postId !== undefined && post !== null) {
            await editVolunteeringPost(parseInt(postId), data.title, data.description);
            alert("Volunteering post edited successfully!");
            navigate(`/volunteeringPost/${parseInt(postId)}`);
          }
          else {
            alert("Error");
          }
        }
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

  useEffect(() => {
    isEdit();
  }, [id])

    return (
      <form className = "create-volunteering-post-form" onSubmit={handleSubmit(contactSubmit)}>
        <h1>{edit ? "Edit Volunteering Post" : "Create Volunteering Post"}</h1>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <label htmlFor="title">Post Title:</label>
          <input
            id="title"
            {...register('title', { 
              required: 'Post title is required',
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
          {errors.title && <p style={{ color: 'red' }}>{errors.title.message}</p>}
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <label htmlFor="description">Post Description:</label>
          <input
            id="description"
            {...register('description', { 
              required: 'Post description is required',
              maxLength: {
                value: 300,
                message: 'Cannot exceed 300 characters'
              }
            })}
          />
          {errors.description && <p style={{ color: 'red' }}>{errors.description.message}</p>}
        </div>

        <button type="submit">{edit ? "Submit Changes" : "Create Volunteering Post"}</button>
      </form>
    );
}

export default CreatePost