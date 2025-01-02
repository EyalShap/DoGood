import { useEffect, useState } from 'react'
import { VolunteeringPostModel } from '../models/VolunteeringPostModel';
import { filterPosts, getAllOrganizationNames, getAllPostsCategories, getAllPostsCities, getAllPostsSkills, getAllVolunteeringNames, getAllVolunteeringPosts, searchByKeywords, sortByLastEditTime, sortByPopularity, sortByPostingTime, sortByRelevance } from '../api/post_api';
import { useNavigate } from 'react-router-dom';

function VolunteeringPostList() {
    const navigate = useNavigate();

    const [posts, setPosts] = useState<VolunteeringPostModel[]>([]);
    const [allPosts, setAllPosts] = useState<VolunteeringPostModel[]>([]);
    const [search, setSearch] = useState("");
    const[sortFunction, setSortFunction] = useState("");
    const[allCategories, setAllCategories] = useState<string[]>([]);
    const[allSkills, setAllSkills] = useState<string[]>([]);
    const[allCities, setAllCities] = useState<string[]>([]);
    const[allOrganizationNames, setAllOrganizationNames] = useState<string[]>([]);
    const[allVolunteeringNames, setAllVolunteeringNames] = useState<string[]>([]);
    const[selectedCategories, setSelectedCategories] = useState<string[]>([]);
    const[selectedSkills, setSelectedSkills] = useState<string[]>([]);
    const[selectedCities, setSelectedCities] = useState<string[]>([]);
    const[selectedOrganizationNames, setSelectedOrganizationNames] = useState<string[]>([]);
    const[selectedVolunteeringNames, setSelectedVolunteeringNames] = useState<string[]>([]);

    const fetchPosts = async () => {
        try {
            let allPosts = await sortByRelevance(await getAllVolunteeringPosts());
            setAllPosts(allPosts);
            setPosts(allPosts);

            let allCategories = await getAllPostsCategories();
            setAllCategories(allCategories);

            let allSkills = await getAllPostsSkills();
            setAllSkills(allSkills);

            let allCities = await getAllPostsCities();
            setAllCities(allCities);

            let allOrganizationNames = await getAllOrganizationNames();
            setAllOrganizationNames(allOrganizationNames);

            let allVolunteeringNames = await getAllVolunteeringNames();
            setAllVolunteeringNames(allVolunteeringNames);
        } catch (e) {
            // send to error page
            alert(e);
        }
    }

    useEffect(() => {
        fetchPosts();
    }, [])

    const handleShowOnClick = (postId: number) => {
        navigate(`/volunteeringPost/${postId}`);
    }

    const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSearch(event.target.value);
    };

    const handleSearchOnClick = async () => {
        try {
            let res : VolunteeringPostModel[] = await searchByKeywords(search, posts);
            setPosts(res);
        }
        catch(e) {
            alert(e);
        }
    }

    const handleSelectionChange = async (event: React.ChangeEvent<HTMLSelectElement>) => {
        const selectedFunction = event.target.value;
        setSortFunction(selectedFunction);
        
        if(selectedFunction === 'relevance') {
            let sorted: VolunteeringPostModel[] = await sortByRelevance(posts);
            setPosts(sorted);
        }
        else if(selectedFunction === 'popularity') {
            let sorted: VolunteeringPostModel[] = await sortByPopularity(posts);
            setPosts(sorted);
        }
        else if(selectedFunction === 'posting time') {
            let sorted: VolunteeringPostModel[] = await sortByPostingTime(posts);
            setPosts(sorted);
        }
        else if(selectedFunction === 'last edit time') {
            let sorted: VolunteeringPostModel[] = await sortByLastEditTime(posts);
            setPosts(sorted);
        }
    };

    const handleCategoriesCheckboxChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const checkedCategory = event.target.value;
        let newSelectedCategories = selectedCategories;
        if(event.target.checked){
            newSelectedCategories = [...selectedCategories, checkedCategory];
            
        }
        else{
            newSelectedCategories = selectedCategories.filter(category => category !== checkedCategory);
        }
        setSelectedCategories(newSelectedCategories);
        await filter(newSelectedCategories, selectedSkills, selectedCities, selectedOrganizationNames, selectedVolunteeringNames);
    }

    const handleSkillsCheckboxChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const checkedSkill = event.target.value;
        let newSelectedSkills = selectedSkills;
        if(event.target.checked){
            newSelectedSkills = [...selectedSkills, checkedSkill];
            
        }
        else{
            newSelectedSkills = selectedSkills.filter(skill => skill !== checkedSkill);
        }
        setSelectedSkills(newSelectedSkills);
        await filter(selectedCategories, newSelectedSkills, selectedCities, selectedOrganizationNames, selectedVolunteeringNames);
    }

    const handleCitiesCheckboxChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const checkedCity = event.target.value;
        let newSelectedCities = selectedCities;
        if(event.target.checked){
            newSelectedCities = [...selectedCities, checkedCity];
            
        }
        else{
            newSelectedCities = selectedCities.filter(city => city !== checkedCity);
        }
        setSelectedCities(newSelectedCities);
        await filter(selectedCategories, selectedSkills, newSelectedCities, selectedOrganizationNames, selectedVolunteeringNames);
    }

    const handleOrganizationNamesCheckboxChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const checkedName = event.target.value;
        let newSelectedNames = selectedOrganizationNames;
        if(event.target.checked){
            newSelectedNames = [...selectedOrganizationNames, checkedName];
        }
        else{
            newSelectedNames = selectedOrganizationNames.filter(name => name !== checkedName);
        }
        setSelectedOrganizationNames(newSelectedNames);
        await filter(selectedCategories, selectedSkills, selectedCities, newSelectedNames, selectedVolunteeringNames);
    }

    const handleVolunteeringNamesCheckboxChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const checkedName = event.target.value;
        let newSelectedNames = selectedVolunteeringNames;
        if(event.target.checked){
            newSelectedNames = [...selectedVolunteeringNames, checkedName];
        }
        else{
            newSelectedNames = selectedVolunteeringNames.filter(name => name !== checkedName);
        }
        setSelectedVolunteeringNames(newSelectedNames);
        await filter(selectedCategories, selectedSkills, selectedCities, selectedOrganizationNames, newSelectedNames);
    }

    const filter = async(categories: string[], skills: string[], cities: string[], organizationNames: string[], volunteeringNames: string[]) => {
        if(categories.length === 0 && skills.length === 0 && cities.length === 0 && organizationNames.length === 0 && volunteeringNames.length === 0) {
            let filtered = allPosts;
            setPosts(filtered);
        }
        else {
            let filtered = await filterPosts(categories, skills, cities, organizationNames, volunteeringNames, posts);
            setPosts(filtered);
        }
    }

    return (
        <div>
            <div className="Posts">
                <h2>Posts</h2>

                <div className="search">
                <label>
                    Search:
                    <input
                    type="text"
                    value={search}
                    onChange={handleSearchChange}
                    />
                </label>

                <button onClick={handleSearchOnClick}>Search</button>
                </div>

                <div className = "sort">
                    <select onChange={handleSelectionChange} value={sortFunction}>
                        <option value = "">Sort by</option>
                        <option value = "relevance">Relevance</option>
                        <option value = "popularity">Popularity</option>
                        <option value = "posting time">Posting time</option>
                        <option value = "last edit time">Last edit time</option>
                    </select>
                </div>

                <div className = "filterByCategory" style = {{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
                <label>Filter by category: </label>
                {allCategories.map((category) => (
                    <div key={category}>
                    <label>
                        <input
                        type="checkbox"
                        value={category}
                        checked={selectedCategories.includes(category)}
                        onChange={handleCategoriesCheckboxChange}
                        />
                        {category}
                    </label>
                    </div>
                ))}
                </div>

                <div className = "filterBySkill" style = {{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
                <label>Filter by skill: </label>
                {allSkills.map((skill) => (
                    <div key={skill}>
                    <label>
                        <input
                        type="checkbox"
                        value={skill}
                        checked={selectedSkills.includes(skill)}
                        onChange={handleSkillsCheckboxChange}
                        />
                        {skill}
                    </label>
                    </div>
                ))}
                </div>

                <div className = "filterByCity" style = {{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
                <label>Filter by city: </label>
                {allCities.map((city) => (
                    <div key={city}>
                    <label>
                        <input
                        type="checkbox"
                        value={city}
                        checked={selectedCities.includes(city)}
                        onChange={handleCitiesCheckboxChange}
                        />
                        {city}
                    </label>
                    </div>
                ))}
                </div>

                <div className = "filterByOrganizationName" style = {{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
                <label>Filter by organization name: </label>
                {allOrganizationNames.map((name) => (
                    <div key={name}>
                    <label>
                        <input
                        type="checkbox"
                        value={name}
                        checked={selectedOrganizationNames.includes(name)}
                        onChange={handleOrganizationNamesCheckboxChange}
                        />
                        {name}
                    </label>
                    </div>
                ))}
                </div>

                <div className = "filterByVolunteeringName" style = {{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
                <label>Filter by volunteering name: </label>
                {allVolunteeringNames.map((name) => (
                    <div key={name}>
                    <label>
                        <input
                        type="checkbox"
                        value={name}
                        checked={selectedVolunteeringNames.includes(name)}
                        onChange={handleVolunteeringNamesCheckboxChange}
                        />
                        {name}
                    </label>
                    </div>
                ))}
                </div>
                
            {posts.length > 0 ? (
                posts.map((post, index) => (
                    <div key={index} className="postItem">
                        <h3>{post.title}</h3>
                        <p>{post.description}</p>
                        <button onClick={() => handleShowOnClick(post.id)}>Show</button>
                    </div>
                ))
            ) : (
                <p>No volunteering posts available.</p>
            )}

            </div>
        </div>
    )
}

export default VolunteeringPostList;