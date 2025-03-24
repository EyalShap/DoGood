import { useForm, SubmitHandler } from 'react-hook-form'; 
import { useParams } from "react-router-dom";
import { useNavigate } from 'react-router-dom';
import { createVolunteeringPost, createVolunteerPost, editVolunteeringPost, editVolunteerPost, getVolunteeringPost, getVolunteerPost } from '../api/post_api';
import React, { useEffect, useState } from 'react';
import { VolunteeringPostModel } from '../models/VolunteeringPostModel';
import './../css/CreateVolunteeringPost.css'
import { PostModel } from '../models/PostModel';

interface PostFormData {
  title: string;
  description: string;
}

function CreatePost() {
  const navigate = useNavigate();
  let { id, postId } = useParams();
  const [edit, setEdit] = useState(false);
  const [post, setPost] = useState<PostModel | null>(null);

  const { register, handleSubmit, formState: { errors } , reset} = useForm<PostFormData>();
  
  const isVolunteeringPost = location.pathname.includes('Volunteering');

  const isEdit = async () => {
    if(postId !== undefined && parseInt(postId) !== -1) {
      setEdit(true);

      let fetchedPost: PostModel;
      if(isVolunteeringPost) {
        fetchedPost = await getVolunteeringPost(parseInt(postId));
        
      }
      else {
        fetchedPost = await getVolunteerPost(parseInt(postId));
      }
      setPost(fetchedPost);
        reset({
          title: fetchedPost.title || '',
          description: fetchedPost.description || ''
        });
      
    }
    else {
      setEdit(false);
      setPost(null);
    }
  }


  const contactSubmit: SubmitHandler<PostFormData> = async (data) => {
    console.log(id);
    try {
      if(id !== undefined) {
        console.log(edit);
        if(!edit) {
            let postId: number = await createVolunteeringPost(data.title, data.description, parseInt(id));
            alert("Post created successfully!");
            navigate(`/volunteeringPostList`);
        }
        else {
          console.log("hiiiiiiiii");
          if(postId !== undefined && post !== null) {
            if(isVolunteeringPost) {
              console.log("im here3");
              await editVolunteeringPost(parseInt(postId), data.title, data.description);
              alert("Post edited successfully!");
              navigate(`/volunteeringPost/${parseInt(postId)}`);
            }
            else {
              console.log("im here4");
              
            }
            
          }
          else {
            alert("Error");
          }
        }
      }
      else {
        if(!edit) {
          let postId: number = await createVolunteerPost(data.title, data.description);
          alert("Post created successfully!");
          navigate(`/volunteeringPostList`);
        }
        else {
          if(postId !== undefined) {
          await editVolunteerPost(parseInt(postId), data.title, data.description);
          alert("Post edited successfully!");
          navigate(`/volunteerPost/${parseInt(postId)}`);
          }
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
        <div className="form-container">
          <div className='createPostHeaders'>
            {isVolunteeringPost && <h1 className="bigHeader">{edit ? "Edit Volunteering Post" : "Create Volunteering Post"}</h1>}
            {!isVolunteeringPost && <h1 className="bigHeader">{edit ? "Edit Volunteer Post" : "Create Volunteer Post"}</h1>}

            <h2 className="smallHeader">Post your volunteering opportunity - connect with others and make a positive impact</h2>
          </div>

          <div className="form-fields">
            <div className="form-group">
              <label className="smallHeader" htmlFor="title">Title:</label>
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
              {errors.title && <p>{errors.title.message}</p>}
            </div>

            <div className="form-group">
              <label className="smallHeader" htmlFor="description">Description:</label>
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
              {errors.description && <p>{errors.description.message}</p>}
            </div>

            <button className='orangeCircularButton' type="submit">
              {edit ? 'Save Changes' : 'Create Post'}
            </button>
          </div>
        </div>
      </form>

    );
}

export default CreatePost