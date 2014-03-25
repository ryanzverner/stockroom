Sequel.migration do
  change do
    create_table(:clients) do
      primary_key :id
      String :name, :size=>200
      DateTime :updated_at
      DateTime :created_at
    end

    create_table(:groups, :ignore_index_errors=>true) do
      primary_key :id
      String :name, :size=>200
      DateTime :updated_at
      DateTime :created_at

      index [:name], :name=>:groups_unique_name, :unique=>true
    end

    create_table(:locations, :ignore_index_errors=>true) do
      primary_key :id
      String :name, :size=>200, :null=>false
      DateTime :created_at, :null=>false
      DateTime :updated_at, :null=>false

      index [:name], :name=>:locations_unique_name, :unique=>true
    end

    create_table(:people, :ignore_index_errors=>true) do
      primary_key :id
      String :first_name, :size=>200
      String :last_name, :size=>200
      String :email, :size=>200
      DateTime :updated_at
      DateTime :created_at

      index [:email]
      index [:first_name]
      index [:last_name]
    end

    create_table(:positions) do
      primary_key :id
      String :name, :size=>200
      DateTime :updated_at
      DateTime :created_at
    end

    create_table(:schema_migrations) do
      String :name, :size=>200
      String :filename, :size=>255, :null=>false
    end

    create_table(:skills) do
      primary_key :id
      String :name, :size=>255
      DateTime :updated_at
      DateTime :created_at
    end

    create_table(:sows) do
      primary_key :id
      Integer :hourly_rate
      DateTime :start
      DateTime :end
      String :url, :size=>255
      DateTime :signed_date
      DateTime :created_at
      DateTime :updated_at
      String :currency_code, :size=>255
    end

    create_table(:users) do
      primary_key :id
      DateTime :updated_at
      DateTime :created_at
      String :name, :size=>200
    end

    create_table(:apprenticeships, :ignore_index_errors=>true) do
      primary_key :id
      foreign_key :person_id, :people, :key=>[:id]
      String :skill_level, :size=>200
      DateTime :start
      DateTime :end
      DateTime :updated_at
      DateTime :created_at

      index [:person_id], :name=>:apprenticeship_fkey_person_id
    end

    create_table(:authentications, :ignore_index_errors=>true) do
      foreign_key :user_id, :users, :key=>[:id]
      String :uid, :size=>200
      String :provider, :size=>200
      DateTime :updated_at
      DateTime :created_at

      index [:user_id], :name=>:authentications_fkey_user_id
      index [:uid, :provider], :name=>:authentications_unique_uid_provider, :unique=>true
    end

    create_table(:employment, :ignore_index_errors=>true) do
      primary_key :id
      DateTime :start
      DateTime :end
      foreign_key :person_id, :people, :key=>[:id]
      foreign_key :position_id, :positions, :key=>[:id]
      DateTime :updated_at
      DateTime :created_at

      index [:person_id], :name=>:employment_fkey_person_id
      index [:position_id], :name=>:employment_fkey_position_id
    end

    create_table(:group_permissions, :ignore_index_errors=>true) do
      String :permission, :default=>"", :size=>200, :null=>false
      foreign_key :group_id, :groups, :default=>0, :null=>false, :key=>[:id]
      DateTime :updated_at
      DateTime :created_at

      primary_key [:permission, :group_id]

      index [:group_id], :name=>:group_permissions_fkey_group_id
    end

    create_table(:group_users, :ignore_index_errors=>true) do
      foreign_key :user_id, :users, :default=>0, :null=>false, :key=>[:id]
      foreign_key :group_id, :groups, :default=>0, :null=>false, :key=>[:id]
      DateTime :updated_at
      DateTime :created_at

      primary_key [:user_id, :group_id]

      index [:user_id], :name=>:group_users_fkey_user_id
    end

    create_table(:mentorships, :ignore_index_errors=>true) do
      primary_key :id
      foreign_key :mentor_id, :people, :key=>[:id]
      foreign_key :apprentice_id, :people, :key=>[:id]
      DateTime :start
      DateTime :end
      DateTime :updated_at
      DateTime :created_at

      index [:apprentice_id], :name=>:mentorship_fkey_apprentice_id
      index [:mentor_id], :name=>:mentorship_fkey_mentor_id
    end

    create_table(:projects, :ignore_index_errors=>true) do
      primary_key :id
      foreign_key :client_id, :clients, :key=>[:id]
      String :name, :size=>200
      DateTime :updated_at
      DateTime :created_at
      String :source_url, :size=>255

      index [:client_id], :name=>:projects_fkey_client_id
    end

    create_table(:director_engagements, :ignore_index_errors=>true) do
      primary_key :id
      foreign_key :person_id, :people, :null=>false, :key=>[:id]
      foreign_key :project_id, :projects, :null=>false, :key=>[:id]
      DateTime :start
      DateTime :end
      DateTime :updated_at
      DateTime :created_at

      index [:person_id], :name=>:director_engagement_fkey_person_id
      index [:project_id], :name=>:director_engagement_fkey_project_id
    end

    create_table(:engagements, :ignore_index_errors=>true) do
      primary_key :id
      Date :start
      Date :end
      foreign_key :employment_id, :employment, :key=>[:id]
      foreign_key :project_id, :projects, :key=>[:id]
      DateTime :updated_at
      DateTime :created_at
      Integer :confidence_percentage, :null=>false

      index [:employment_id], :name=>:engagements_fkey_employment_id
      index [:project_id], :name=>:engagements_fkey_project_id
    end

    create_table(:location_memberships, :ignore_index_errors=>true) do
      primary_key :id
      foreign_key :location_id, :locations, :key=>[:id]
      DateTime :start, :null=>false
      DateTime :created_at, :null=>false
      DateTime :updated_at, :null=>false
      foreign_key :employment_id, :employment, :key=>[:id]

      index [:employment_id], :name=>:location_memberships_fkey_employment_id
      index [:location_id], :name=>:location_memberships_fkey_location_id
    end

    create_table(:project_skills, :ignore_index_errors=>true) do
      primary_key :id
      foreign_key :project_id, :projects, :key=>[:id]
      foreign_key :skill_id, :skills, :key=>[:id]
      DateTime :updated_at
      DateTime :created_at

      index [:project_id], :name=>:project_skills_fkey_project_id
      index [:skill_id], :name=>:project_skills_fkey_skill_id
    end

    create_table(:project_sows, :ignore_index_errors=>true) do
      primary_key :id
      foreign_key :project_id, :projects, :key=>[:id]
      foreign_key :sow_id, :sows, :key=>[:id]
      DateTime :updated_at
      DateTime :created_at

      index [:project_id], :name=>:project_sows_fkey_project_id
      index [:sow_id], :name=>:project_sows_fkey_sow_id
    end
  end
end
